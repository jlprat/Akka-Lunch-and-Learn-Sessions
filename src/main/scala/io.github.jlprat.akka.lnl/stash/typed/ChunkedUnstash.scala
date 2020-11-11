package io.github.jlprat.akka.lnl.stash.typed

import scala.concurrent.duration._
import scala.io.StdIn

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout

object ChunkedUnstash {

  case class Done(primes: List[Int])

  sealed trait Command
  case object Initialize                                          extends Command
  case class Primes(numberOfPrimes: Int, replyTo: ActorRef[Done]) extends Command
  private case object ResumeUnstash                               extends Command
  private case class Stashed(msg: Command)                        extends Command

  def primeStream(s: LazyList[Int]): LazyList[Int] =
    LazyList.cons(s.head, primeStream(s.tail filter { _ % s.head != 0 }))

  def apply(): Behavior[Command] =
    Behaviors.withStash(300) { stashBuffer =>
      def uninitialized(): Behavior[Command] =
        Behaviors.receive {
          case (context, Initialize) =>
            context.log.info("Initializing - doing some costly things")
            if (stashBuffer.size > 0) {
              context.self.tell(ResumeUnstash)
              stashBuffer.unstash(initialized(), 5, Stashed)
            } else {
              initialized()
            }
          case (context, msg @ Primes(numberOfPrimes, _)) =>
            context.log.info("Stashing request to calculate {} primes", numberOfPrimes)
            stashBuffer.stash(msg)
            Behaviors.same
          case _ => Behaviors.unhandled
        }

      def initialized(): Behavior[Command] =
        Behaviors.receive {
          case (context, Stashed(Primes(numberOfPrimes, replyTo))) =>
            context.log.info("Processing a previously stashed message")
            val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
            replyTo.tell(Done(nPrimes.toList))
            Behaviors.same
          case (context, Primes(numberOfPrimes, replyTo)) =>
            context.log.info("Processing a fresh new message")
            val nPrimes = primeStream(LazyList.from(2)).take(numberOfPrimes)
            replyTo.tell(Done(nPrimes.toList))
            Behaviors.same
          case (context, ResumeUnstash) =>
            if (stashBuffer.size > 0) {
              context.log.info("Finished one batch of unstashing")
              context.self.tell(ResumeUnstash)
              stashBuffer.unstash(initialized(), 5, Stashed)
            } else {
              context.log.info("Finished Unstashing")
              Behaviors.same
            }
          case (context, Stashed(msg)) =>
            context.log.error("Wrong message in stash {}", msg)
            throw new IllegalStateException("Wrong message in stash")
          case (_, Initialize) => throw new IllegalStateException("Already initialized")
        }

      uninitialized()
    }

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Command] =
      ActorSystem(ChunkedUnstash(), "ChunkedStashSystem")

    implicit val timeout: Timeout = Timeout(100.millis)
    implicit val ec               = system.executionContext

    // We fill the stack
    (1 to 30).foreach { _ =>
      system
        .ask(ref => Primes(40, ref))
        .foreach(done => println(s"Prime Calculated ${done.primes.mkString(",")}"))
    }

    // We initialize the system
    system.tell(Initialize)

    system
      .ask(ref => Primes(100, ref))
      .foreach(done => println(s"Should not be printed last ${done.primes.mkString(",")}"))

    StdIn.readLine("Press RETURN to stop...\n")

    val _ = system.terminate()
  }
}
