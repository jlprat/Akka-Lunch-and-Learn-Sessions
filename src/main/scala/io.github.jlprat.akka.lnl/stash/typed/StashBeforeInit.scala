package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

/**
  * Class Showcasing the how to stash messages received before the actor is properly initialized
  */
object StashBeforeInit {

  sealed trait Status
  case class Done(primes: List[Int]) extends Status
  case object Processing             extends Status

  sealed trait Command
  case object Initialize                                            extends Command
  case class Primes(numberOfPrimes: Int, replyTo: ActorRef[Status]) extends Command

  def primeStream(s: LazyList[Int]): LazyList[Int] =
    LazyList.cons(s.head, primeStream(s.tail filter { _ % s.head != 0 }))

  def apply(): Behavior[Command] =
    Behaviors.withStash(25) {
      stashBuffer => // Only 25 messages can be stashed here, if more, the Actor will throw an Exception
        Behaviors.receive {
          case (context, Initialize) =>
            context.log.info("Initializing - doing some costly things")
            // All messages in the stash will be prepended (put in front) of the Mailbox
            // The bigger the stash buffer it is, the longer it will take to consume those messages
            stashBuffer.unstashAll(initialized())
          case (context, msg @ Primes(numberOfPrimes, _)) =>
            context.log.info("Stashing request to calculate {} primes", numberOfPrimes)
            // Stashing the message for a later processing
            stashBuffer.stash(msg)
            Behaviors.same
        }
    }

  def initialized(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Primes(numberOfPrimes, replyTo) =>
        replyTo.tell(Processing)
        // We could store this already calculated primes in a field,
        // but for the sake of performing something costly, we calculate it every time
        val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
        replyTo.tell(Done(nPrimes.toList))
        Behaviors.same
      case Initialize => throw new IllegalStateException("Already initialized")
    }
}
