package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
  * Class Showcasing the basic behavior of failing or dropping messages when Actor is not yet initialized.
  */
object DropBeforeInit {

  sealed trait Status
  case class Done(primes: List[Int]) extends Status
  case object Processing             extends Status
  case object Discarded              extends Status

  sealed trait Command
  case object Initialize                                            extends Command
  case class Primes(numberOfPrimes: Int, replyTo: ActorRef[Status]) extends Command

  /**
    * Generates a "Stream" of primes
    */
  def primeStream(s: LazyList[Int]): LazyList[Int] =
    LazyList.cons(s.head, primeStream(s.tail filter { _ % s.head != 0 }))

  def apply(): Behavior[Command] =
    Behaviors.receive {
      case (context, Initialize) =>
        context.log.info("Initializing - doing some costly things")
        initialized()
      case (context, Primes(numberOfPrimes, replyTo)) =>
        // Message could be left Unhandled, ignored or it could throw an Exception
        context.log.error("Request to calculate {} primes was discarded", numberOfPrimes)
        replyTo.tell(Discarded)
        Behaviors.same
    }

  def initialized(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Primes(numberOfPrimes, replyTo) =>
        replyTo.tell(Processing)
        // The lazy list of primes could be saved in the actor for performance reasons
        // In this particular example we want the actor to perform costly operations
        val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
        replyTo.tell(Done(nPrimes.toList))
        Behaviors.same
      case Initialize => throw new IllegalStateException("Already initialized")
    }
}
