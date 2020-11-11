package io.github.jlprat.akka.lnl.supervision.classic

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import io.github.jlprat.akka.lnl.supervision.classic.Shutdown._

object Shutdown {

  sealed trait Command
  case object Init             extends Command
  case object GracefulShutdown extends Command

  def props(): Props = Props(classOf[Shutdown])
}

class Shutdown extends Actor with ActorLogging {

  override def receive: Actor.Receive = {
    case Init => spawnChildren()
      context.become(initialized)
    case GracefulShutdown =>
      log.error("I'm not yet initialized!")
  }

  def initialized: Actor.Receive = {
    case Init => log.error("I'm already initialized!")
    case GracefulShutdown =>
      log.info("Pre Cleaning Up Tasks")
      // After this actor is stopped, all children will also be stopped
      context.stop(self)
  }

  override def postStop(): Unit = {
    log.info("Post Clean Up Tasks")
  }

  def spawnChildren(): Unit = ()
}
