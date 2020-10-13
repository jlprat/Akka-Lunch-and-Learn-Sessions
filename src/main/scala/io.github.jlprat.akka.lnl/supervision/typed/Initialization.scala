package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Initialization {
  sealed trait Command
  case object Init extends Command

  def apply(): Behavior[Command] = Behaviors.setup{ _ =>

    // Setup hook
    initDB()

    Behaviors.receiveMessage{
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
