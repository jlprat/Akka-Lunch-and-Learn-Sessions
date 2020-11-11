package io.github.jlprat.akka.lnl.stash.classic

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.Stash
import io.github.jlprat.akka.lnl.stash.classic.StashBeforeInit._

object StashBeforeInit {

  sealed trait Status
  case class Done(primes: List[Int]) extends Status
  case object Processing             extends Status

  sealed trait Command
  case object Initialize                 extends Command
  case class Primes(numberOfPrimes: Int) extends Command

  def props(): Props = Props(classOf[StashBeforeInit])

  private def primeStream(s: LazyList[Int]): LazyList[Int] =
    LazyList.cons(s.head, primeStream(s.tail filter { _ % s.head != 0 }))

}

class StashBeforeInit extends Actor with Stash with ActorLogging {

  override def receive: Actor.Receive = {
    case Initialize =>
      log.info("Initializing - doing some costly things")
      unstashAll()
      context.become(initialized)
    case Primes(numberOfPrimes) =>
      log.info("Stashing request to calculate {} number of primes", numberOfPrimes)
      stash()
  }

  def initialized: Actor.Receive = {
    case Initialize =>
      throw new IllegalStateException("Already initialized")
    case Primes(numberOfPrimes) =>
      sender() ! Processing
      // We could store this already calculated primes in a field,
      // but for the sake of performing something costly, we calculate it every time
      val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
      sender() ! Done(nPrimes.toList)
  }
}
