package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors

/**
  * Behavior showcasing the special `watchWith` method
  */
object WatchingAlt {

  sealed trait JobState
  case object Finished extends JobState

  sealed trait Command
  case class StartJob(code: String, replyTo: ActorRef[JobState]) extends Command
  case class FinishedJob(replyTo: ActorRef[JobState]) extends Command

  def apply(): Behavior[Command] =
    Behaviors
      .receive[Command] {
        case (context, StartJob(code, replyTo)) =>
          val child = context.spawnAnonymous[Nothing](job(code))
          //When `child` is finished this behavior will receive the given message
          context.watchWith(child, FinishedJob(replyTo))
          Behaviors.same
        case (_, FinishedJob(replyTo)) =>
          replyTo ! Finished
          Behaviors.same
      }

  def job(code: String): Behavior[Nothing] =
    Behaviors.setup[Nothing] { context =>
      context.log.info(s"Running job $code")
      Behaviors.stopped
    }
}
