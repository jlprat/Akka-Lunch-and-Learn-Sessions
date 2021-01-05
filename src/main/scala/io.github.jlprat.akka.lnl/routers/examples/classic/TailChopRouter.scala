package io.github.jlprat.akka.lnl.routers.examples.classic

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.util.Timeout

import TailChopRouter.Worker.GetRelatedArtist
import akka.routing.FromConfig

/**
  * This uses TailChop strategy.
  * This service finds related artists for the given one.
  * For simplicity reasons, the related artist is just a String reversal.
  * The work done to find those related artists might be a DB query, on a cluster system.
  * Let's assume each query fired on this DB will, with high probability, land on a different
  * node of the cluster.
  * Some times, a query is cogged with latency, as the node queried is busy, in here simulated
  * by a random check and a `Thread.sleep`.
  * TailChop strategy attempts, at resources cost, to lower the latency by firing redundant
  * queries after a given delay time.
  * If the DB replies within their normal time range (10ms in this case) no further queries
  * will be fired. Otherwise, an additional query will be fired in hopes it doesn't also hit a
  * busy node. In this particular case, if this additional query is also not replying within
  * their normal range, it also fires another redundant query.
  * 
  * To see where this strategy shines, look for log lines where the response time is < 400ms
  * some of those, might have an extra log line stating they hit some latency.
  */
object TailChopRouter {

  object Worker {
    trait Command
    case class GetRelatedArtist(artist: String) extends Command

    def props(): Props = Props(classOf[Worker])
  }

  class Worker extends Actor with ActorLogging {

    println(self.path.toStringWithoutAddress)

    /**
      * Let's pretend this does something
      */
    private def digInHistoryFindingASimilarArtist(
        artist: String,
        logger: LoggingAdapter
    ): String = {
      logger.info("Finding a similar artist to {}", artist)
      if (Math.random() >= 0.5) {
        logger.info("I hit some latency trying to find a similar for {}!", artist)
        Thread.sleep(400)
      }
      artist.reverse.toLowerCase.capitalize
    }

    override def receive: Actor.Receive = {
      case GetRelatedArtist(artist) =>
        // Let's pretend this algorithm is really prone to have high latency spikes
        sender() ! digInHistoryFindingASimilarArtist(artist, log)
    }

  }

  def main(args: Array[String]): Unit = {
    Thread.sleep(4.seconds.toMillis)
    implicit val system  = ActorSystem("TailChopping")
    implicit val timeout = Timeout(1.second)

    //Reads type of router and config from "application.conf"
    val router = system.actorOf(FromConfig.props(Props[Worker]()), "tailChopRouter")

    val artists = Seq("The Who", "Queen", "Metallica", "Green Day", "Bad Religion")

    artists.foreach { artist =>
      val startTime = System.currentTimeMillis()
      val other     = Await.result(router.ask(GetRelatedArtist(artist)).mapTo[String], 1.second)
      val endTime   = System.currentTimeMillis() - startTime
      // if endTime is lower than 400ms and you see
      // "I hit some latency trying to find a similar for" in the logs,
      // it means Tail Chopping saved a long latency!
      println(
        s"If you liked $artist, you might like $other ($endTime)"
      )
    }

    Await.result(system.terminate(), 1.second)

    println("Shutting down!")
  }
}
