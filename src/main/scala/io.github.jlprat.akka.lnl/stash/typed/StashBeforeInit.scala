package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object StashBeforeInit {

  sealed trait Status
  case class Done(primes: List[Int]) extends Status
  case object Processing             extends Status
  
  sealed trait Command
  case object Initialize                                            extends Command
  case class Primes(numberOfPrimes: Int, replyTo: ActorRef[Status]) extends Command

  def primeStream(s: LazyList[Int]): LazyList[Int] =
    LazyList.cons(s.head, primeStream(s.tail filter { _ % s.head != 0 }))

  def apply(): Behavior[Command] = Behaviors.withStash(25) { stashBuffer =>  
    Behaviors.receive {
      case (context, Initialize) =>
        context.log.info("Initializing - doing some costly things")
        stashBuffer.unstashAll(initialized())
      case (context, msg @ Primes(numberOfPrimes, _)) =>
        context.log.error("Stashing request to calculate {} number of primes", numberOfPrimes)
        stashBuffer.stash(msg)
        Behaviors.same
    }
  }

  def initialized(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Primes(numberOfPrimes, replyTo) =>
        replyTo.tell(Processing)
        val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
        replyTo.tell(Done(nPrimes.toList))
        Behaviors.same
      case Initialize => throw new IllegalStateException("Already initialized")
    }
}
