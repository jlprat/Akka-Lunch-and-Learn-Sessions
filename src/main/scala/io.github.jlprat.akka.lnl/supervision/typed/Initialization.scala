package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.SupervisorStrategy
import scala.annotation.nowarn
import akka.actor.typed.scaladsl.ActorContext

object Initialization {
  sealed trait Command
  case object Init extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { _ =>
      // Setup hook
      initDB()

      Behaviors.receiveMessage {
        case Init =>
          // Initialize via message
          initDB()
          Behaviors.same
        case _ => //do normal things
          Behaviors.same
      }
    }

  private def initDB(): Unit = ()
}

object Restart {
  sealed trait Command
  case object DoThings extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      // This will be executed on Start
      createChildren(context)
      Behaviors.supervise {
        // This code will be executed on Start and Restart
        Behaviors.receiveMessage[Command] {
          case DoThings =>
            //Do things
            Behaviors.same
        }
      }.onFailure[Exception](SupervisorStrategy.restart)
    }

  @nowarn  
  def createChildren(context: ActorContext[Command]): Unit = ()
}
