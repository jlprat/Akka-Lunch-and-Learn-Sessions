package io.github.jlprat.akka.lnl.routers.examples.classic

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.routing.FromConfig
import akka.util.Timeout
import io.github.jlprat.akka.lnl.routers.examples.classic.ScatterGatherFirstCompletedRouter.Worker.GetRelatedArtist

/**
  * This uses ScatterGatherFirstCompleted strategy
  * This service finds related artists for the given one.
  * For simplicity reasons, the related artist is just a String reversal.
  * The work done to find those related artists might be a DB query, on a cluster system.
  * Let's assume each query fired on this DB will, with high probability, land on a different
  * node of the cluster.
  * The query lasts for some time, here simulated with a random number.
  * The message is broadcasted to all routees and as soon as one of them responds to the
  * router, the reply is sent back to the original sender.
  * In the logs we can see that for the first artist, 3 queries are started, and the response to
  * the end client is done right after the first routee that completes.
  * We can also see looking at the logs, that routees might still be busy with older queries
  * when a new one comes in.
  */
object ScatterGatherFirstCompletedRouter {
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
      logger.info("({}) Finding a similar artist to {}", self.path.name, artist)
      val waitTime = (Math.random() * 100).toLong
      logger.info("({}) - ({}) Waiting {} ms", self.path.name, artist, waitTime)
      Thread.sleep(waitTime)
      artist.reverse.toLowerCase.capitalize
    }

    override def receive: Actor.Receive = {
      case GetRelatedArtist(artist) =>
        // Let's pretend this algorithm is really prone to have high latency spikes
        sender() ! s"${digInHistoryFindingASimilarArtist(artist, log)} - Served by: ${self.path.toStringWithoutAddress}."
    }

  }

  def main(args: Array[String]): Unit = {
    Thread.sleep(4.seconds.toMillis)
    implicit val system  = ActorSystem("ScatterGather")
    implicit val timeout = Timeout(1.second)

    //Reads type of router and config from "application.conf"
    val router = system.actorOf(FromConfig.props(Worker.props()), "scatterGatherRouter")

    val artists = Seq("The Who", "Queen", "Metallica", "Green Day", "Bad Religion")

    artists.foreach { artist =>
      val startTime = System.currentTimeMillis()
      val other     = Await.result(router.ask(GetRelatedArtist(artist)).mapTo[String], 1.second)
      val endTime   = System.currentTimeMillis() - startTime
      // if endTime is lower than 400ms and you see
      // "I hit some latency trying to find a similar for" in the logs,
      // it means Tail Chopping saved a long latency!
      println(
        s"If you liked $artist, you might like $other ($endTime ms)"
      )
    }

    Await.result(system.terminate(), 1.second)

    println("Shutting down!")
  }
}
