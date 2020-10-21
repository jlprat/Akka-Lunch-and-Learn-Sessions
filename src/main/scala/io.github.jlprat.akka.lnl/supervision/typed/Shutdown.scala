package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{Behavior, PostStop}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.ActorRef

object Shutdown {
  sealed trait Command
  case object Init             extends Command
  case object GracefulShutdown extends Command

  def apply(): Behavior[Command] =
    Behaviors.receive {
      case (context, Init) =>
        spawnChildren(context)
        initialized()
      case (context, _) =>
        context.log.error("I'm not initialized!")
        Behaviors.same
    }

  def initialized(): Behavior[Command] =
    Behaviors
      .receive[Command] {
        case (context, GracefulShutdown) =>
          context.log.info("Pre Cleaning Up Tasks")
          // After this actor is stopped, all children will also be stopped
          Behaviors.stopped
        case (context, Init) =>
          context.log.error("I'm already initialized")
          Behaviors.same
      }
      .receiveSignal {
        case (context, PostStop) =>
          context.log.info("Post Cleaning Up Tasks")
          Behaviors.same
      }

  def spawnChildren(context: ActorContext[Command]): ActorRef[ChildCommand] = {
    context.spawn(child, "child")
  }

  sealed trait ChildCommand
  case object NoOp extends ChildCommand

  val child: Behavior[ChildCommand] = Behaviors.receiveMessage[ChildCommand] {
    case NoOp => Behaviors.same
  }.receiveSignal{
    case (context, PostStop) =>
        context.log.info("Child Post Cleaning Up Tasks")
        Behaviors.same
  }
}
