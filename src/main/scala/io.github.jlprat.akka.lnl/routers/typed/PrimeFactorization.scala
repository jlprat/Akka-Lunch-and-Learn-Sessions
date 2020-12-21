package io.github.jlprat.akka.lnl.routers.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import scala.annotation.tailrec

object PrimeFactorization {

  sealed trait Command
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

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage[Command] {
        case PrimeFactor(n) =>
          val init = System.currentTimeMillis()
          context.log.info(
            "Primes for {} is {} ({}ms)",
            n,
            primeFactors(n, Seq.empty).mkString(", "),
            System.currentTimeMillis() - init
          )
          Behaviors.same
      }
    }

}
