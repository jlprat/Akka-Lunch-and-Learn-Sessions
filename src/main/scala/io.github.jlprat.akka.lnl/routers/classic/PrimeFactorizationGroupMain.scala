package io.github.jlprat.akka.lnl.routers.classic

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.Await
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorization.PrimeFactor
import akka.routing.RoundRobinGroup

object PrimeFactorizationGroupMain {

  def main(args: Array[String]): Unit = {
    val toFactor = Seq(4934578352334L, 196330801L, 4934578352334L, 196330801L, 4934578352334L,
      196330801L, 4934578352334L, 196330801L, 4934578352334L, 196330801L, 4934578352334L,
      196330801L, 4934578352334L, 196330801L)

    val system = ActorSystem("PrimeFactorization")

    val actors = (1 to 5).map { i =>
      system.actorOf(PrimeFactorization.props(), s"routee$i")
    }.toList

    val paths = actors.map(_.path.toStringWithoutAddress)

    paths.foreach(println(_))

    val group = system.actorOf(RoundRobinGroup(paths).props(), "PrimeFactorizationGroupRouter")

    toFactor.foreach { number =>
      group ! PrimeFactor(number)
    }

    println("Sleeping for 10 seconds")
    Thread.sleep(10.seconds.toMillis)
    println("Terminating")
    val _ = Await.result(system.terminate(), 3.seconds)
  }
}
