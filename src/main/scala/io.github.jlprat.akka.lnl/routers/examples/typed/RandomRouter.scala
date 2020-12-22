package io.github.jlprat.akka.lnl.routers.examples.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.ActorSystem

import scala.concurrent.duration._
import io.github.jlprat.akka.lnl.routers.typed.PrimeFactorization

/**
  * This uses Random strategy.
  */
object RandomRouter {

  def main(args: Array[String]): Unit = {

    val toFactor = Seq(4934578352334L, 196330801L, 217997299L, 231282467L, 4934578352334L,
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L,
      4934578352334L, 196330801L, 217997299L, 231282467L)

    val pool = Routers.pool(4) {
      Behaviors.supervise(PrimeFactorization()).onFailure(SupervisorStrategy.restart)
    }.withRandomRouting()

    val router = ActorSystem[PrimeFactorization.Command](pool, "PrimeFactorization")

    toFactor.foreach { n =>
      router.tell(PrimeFactorization.PrimeFactor(n))
    }

    println("App will shutdown in 10 seconds")
    Thread.sleep(10.seconds.toMillis)

    println("Terminating!")
    router.terminate()

  }
}
