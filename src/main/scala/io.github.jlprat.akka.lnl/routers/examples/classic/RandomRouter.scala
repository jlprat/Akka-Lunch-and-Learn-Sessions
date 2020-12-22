package io.github.jlprat.akka.lnl.routers.examples.classic

import akka.actor.ActorSystem
import akka.routing.BalancingPool
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorization
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorization.PrimeFactor

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.routing.RandomPool

/**
  * This uses Random strategy.
  * There is no way we can predict how work will be split among routees.
  */
object RandomRouter {

  def main(args: Array[String]): Unit = {

    val toFactor = Seq(4934578352334L, 196330801L, 217997299L, 231282467L, 4934578352334L,
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L,
      4934578352334L, 196330801L, 217997299L, 231282467L)

    val system = ActorSystem("PrimeFactorization")

    val router =
      system.actorOf(RandomPool(4).props(PrimeFactorization.props()), "PrimeFactorizationRouter")

    toFactor.foreach { number =>
      router ! PrimeFactor(number)
    }

    println("Sleeping for 10 seconds")
    Thread.sleep(10.seconds.toMillis)
    println("Terminating")
    val _ = Await.result(system.terminate(), 3.seconds)
  }

}
