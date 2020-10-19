package io.github.jlprat.akka.lnl.supervision.classic

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import io.github.jlprat.akka.lnl.supervision.classic.Watching.Parent.StartJob

object Watching {

  object Parent {
    sealed trait Command
    case class StartJob(code: String) extends Command

    def props(): Props = Props(classOf[Parent])
  }

  class Parent extends Actor with ActorLogging {

    var jobs: Map[ActorRef, ActorRef] = Map.empty

    override def receive: Actor.Receive = {
      case StartJob(code) =>
        val child = context.actorOf(Child.props(code))
        val _     = context.watch(child)
        jobs = jobs + (child -> sender())
      case Terminated(ref) =>
        jobs(ref) ! "Done"

    }

  }

  object Child {
    def props(code: String): Props = Props(classOf[Child], code)
  }

  class Child(val code: String) extends Actor with ActorLogging {

    log.info("Working!")

    context.stop(self)

    override def receive: Actor.Receive = {
      case _ =>
    }
  }
}
