package io.github.jlprat.akka.lnl.supervision.classic

import akka.actor.Actor
import akka.actor.Props
import io.github.jlprat.akka.lnl.supervision.classic.Initialization._

object Initialization {
  sealed trait Command
  case object Init extends Command

  object DBConnection

  def props(): Props = Props(classOf[Initialization])
}

class Initialization extends Actor {

  // Constructor initialization
  initDB()

  // Pre Start Hook initialization
  override def preStart(): Unit = {
    initDB()
  }

  override def receive: Actor.Receive = {
    case Init =>
      // Initialization via Message
      initDB()
    case _ => ??? // normal actor work
  }

  private def initDB(): Unit = ()

}

class Restart extends Actor {

  override def preStart(): Unit = {
    // We create our children, but we want to keep them on restarts
    initChildren()
  }

  /**
    * default implementation:
    *
    * def preRestart(@unused reason: Throwable, @unused message: Option[Any]): Unit = {
    *   context.children.foreach { child =>
    *     context.unwatch(child)
    *     context.stop(child)
    *   }
    *   postStop()
    * }
    */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    // We don't stop our children anymore!
    postStop()
  }

  /**
    * Default implementation
    * def postRestart(@unused reason: Throwable): Unit = {
    *   preStart()
    * }
    */
  override def postRestart(reason: Throwable): Unit = {
    // we do not call `preStart` so no new children are created
  }

  override def receive: Actor.Receive = ???

  def initChildren(): Unit = ()

}
