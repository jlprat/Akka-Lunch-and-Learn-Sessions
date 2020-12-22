package io.github.jlprat.akka.lnl.routers.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.ActorSystem

import scala.concurrent.duration._

/**
  * This uses RoundRobin strategy.
  * Number to factor are coming in groups of 4 (same as routees),
  * and they are 2 complex and 2 simple numbers.
  * This means, with RoundRobin, 3rd and 4th Routee finish their job earlier and idle,
  * meanwhile, 1st and 2nd, will be all the time busy calculating prime factorization.
  */
object PrimeFactorizationMain {

  def main(args: Array[String]): Unit = {

    val toFactor = Seq(4934578352334L, 196330801L, 217997299L, 231282467L, 4934578352334L,
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L,
      4934578352334L, 196330801L, 217997299L, 231282467L)

    val pool = Routers.pool(4) {
      Behaviors.supervise(PrimeFactorization()).onFailure(SupervisorStrategy.restart)
    }

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
