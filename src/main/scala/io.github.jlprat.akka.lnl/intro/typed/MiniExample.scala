package io.github.jlprat.akka.lnl.intro.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object MiniExample {

  sealed trait Command
  case object Click                                  extends Command
  case class RetrieveClicks(replyTo: ActorRef[Long]) extends Command

  def apply(): Behavior[Command] = counting(0)

  private def counting(count: Long): Behavior[Command] =
    Behaviors.receiveMessage {
      case Click => counting(count + 1)
      case RetrieveClicks(replyTo) =>
        replyTo ! count
        Behaviors.same
    }
}
