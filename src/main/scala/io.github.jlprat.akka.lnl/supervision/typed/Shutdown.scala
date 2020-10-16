package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{Behavior, PostStop}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

import scala.annotation.nowarn

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

  @nowarn
  def spawnChildren(context: ActorContext[Command]): Unit = ()
}
