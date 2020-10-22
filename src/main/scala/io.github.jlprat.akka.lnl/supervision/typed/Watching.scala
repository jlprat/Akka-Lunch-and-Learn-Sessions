package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{ActorRef, Behavior, ChildFailed, Terminated}
import akka.actor.typed.scaladsl.Behaviors

/**
  * Behavior showcasing how to watch actor termination states
  */
object Watching {

  sealed trait JobState
  case object Finished extends JobState
  case object Failed   extends JobState

  sealed trait Command
  case class StartJob(code: String, replyTo: ActorRef[JobState]) extends Command

  var jobs: Map[ActorRef[Nothing], ActorRef[JobState]] = Map.empty

  def apply(): Behavior[Command] =
    Behaviors
      .receive[Command] {
        case (context, StartJob(code, replyTo)) =>
          val child = context.spawnAnonymous[Nothing](job(code))
          context.watch(child)
          jobs = jobs + (child -> replyTo)
          Behaviors.same
      }
      .receiveSignal {
        //this is received upon actor failure
        case (_, ChildFailed(ref, _)) =>
          jobs(ref).tell(Failed)
          jobs = jobs - ref
          Behaviors.same
        //this is received upon actor Termination
        case (_, Terminated(ref)) =>
          jobs(ref).tell(Finished)
          jobs = jobs - ref
          Behaviors.same
      }

  def job(code: String): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      context.log.info(s"Running job $code")
      Behaviors.stopped
    }
}
