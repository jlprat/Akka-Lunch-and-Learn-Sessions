package io.github.jlprat.akka.lnl.routers.classic

import akka.actor.ActorSystem
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorizationRouter.PrimeFactor

import scala.concurrent.duration._
import scala.concurrent.Await

object PrimeFactorizationMain {

  def main(args: Array[String]): Unit = {
    val toFactor = Seq(4934578352334L, 4934578352334L, 4934578352334L, 4934578352334L,
      4934578352334L, 4934578352334L, 4934578352334L, 4934578352334L, 4934578352334L,
      4934578352334L, 4934578352334L, 4934578352334L, 4934578352334L, 4934578352334L)

    val system = ActorSystem()

    val router = system.actorOf(PrimeFactorizationRouter.props())

    toFactor.foreach { number =>
      router ! PrimeFactor(number)
    }

    println("Sleeping for 10 seconds")
    Thread.sleep(10.seconds.toMillis)
    println("Terminating")
    val _ = Await.result(system.terminate(), 3.seconds)
  }
}
