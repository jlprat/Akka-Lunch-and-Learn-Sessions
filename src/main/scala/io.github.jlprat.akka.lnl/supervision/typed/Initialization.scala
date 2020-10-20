package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}

object Initialization {
  sealed trait Command
  case object Init extends Command
  case object DoThings extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      // Setup hook
      initDB(context)
      Behaviors.receiveMessage {
        case Init =>
          Behaviors.same
        case DoThings => //do normal things
          Behaviors.same
      }
    }

  def withInitMessage(): Behavior[Command] = Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Init =>
          // Initialize via message
          initDB(context)
          Behaviors.same
        case DoThings => //do normal things
          Behaviors.same
      }
    }

  private def initDB(context: ActorContext[Command]): Unit = {
    context.log.info("DB initialized")
  }
}

object Restart {
  sealed trait Command
  case object DoThings extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      // This will be executed on Start
      createChildren(context)
      Behaviors
        .supervise {
          // This code will be executed on Start and Restart
          Behaviors.receiveMessage[Command] {
            case DoThings =>
              //Do things
              Behaviors.same
          }
        }
        .onFailure[Exception](SupervisorStrategy.restart)
    }

  def createChildren(context: ActorContext[Command]): ActorRef[ChildCommand] = {
    context.spawn(child(), "child")
  }

  sealed trait ChildCommand
  case object NoOp extends ChildCommand

  def child(): Behavior[ChildCommand] =
    Behaviors.receiveMessage {
      case NoOp => Behaviors.same
    }
}
