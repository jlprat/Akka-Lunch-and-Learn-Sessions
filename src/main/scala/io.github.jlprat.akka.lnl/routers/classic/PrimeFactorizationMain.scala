package io.github.jlprat.akka.lnl.routers.classic

import akka.actor.ActorSystem

import scala.concurrent.duration._
import scala.concurrent.Await
import akka.routing.RoundRobinPool
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorization.PrimeFactor

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
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L,
      231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L)

    val system = ActorSystem("PrimeFactorization")

    val router = system.actorOf(RoundRobinPool(4).props(PrimeFactorization.props()), "PrimeFactorizationRouter")

    toFactor.foreach { number =>
      router ! PrimeFactor(number)
    }

    println("Sleeping for 10 seconds")
    Thread.sleep(10.seconds.toMillis)
    println("Terminating")
    val _ = Await.result(system.terminate(), 3.seconds)
  }
}
