package io.github.jlprat.akka.lnl.routers.classic

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import scala.annotation.tailrec

import PrimeFactorization._

object PrimeFactorization {

  trait Command
  case class PrimeFactor(n: Long) extends Command

  /**
    * Gives the prime factors of the given number
    * @param n remaining of the number to factorize
    * @param primes stream of primes
    * @param factors already calculated factors
    * @return
    */
  @tailrec
  def primeFactors(n: Long, factors: Seq[Long]): Seq[Long] = {
    if (n == 1) factors.reverse
    else {
      // This loop is not a Range because Long rages exceed the maximum length
      var i = 2L
      while (!(n % i == 0)) {
        i = i + 1
      }
      primeFactors(n / i, i +: factors)
    }
  }

  def props(): Props = Props.create(classOf[PrimeFactorization])
}

class PrimeFactorization extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case PrimeFactor(n) =>
      val init = System.currentTimeMillis()
      log.info(
        "Primes for {} is {} ({}ms)",
        n,
        primeFactors(n, Seq.empty).mkString(", "),
        System.currentTimeMillis() - init
      )
  }
}
