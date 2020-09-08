package io.github.jlprat.akka.lnl.intro.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import io.github.jlprat.akka.lnl.intro.util.Util

/**
  * Object that contains the TemperatureStatistics behavior
  */
object TemperatureStatistics {

  //Trait that models all possible messages that this actor can handle
  sealed trait Command
  case class TemperatureReading(value: Double)                extends Command
  case class GetMaxTemperature(replyTo: ActorRef[Double])     extends Command
  case class GetMinTemperature(replyTo: ActorRef[Double])     extends Command
  case class GetAverageTemperature(replyTo: ActorRef[Double]) extends Command

  def apply(testMode: Boolean = false): Behavior[Command] =
    Behaviors.setup { context =>
      context.spawn(TemperatureGatherer(context.self, Util.getTemperature, testMode), "TempGatherer")
      Behaviors.receiveMessage {
        case TemperatureReading(value) =>
          context.log.info(s"Temperature received $value")
          withValues(value, value, value, 1)
        case _ => Behaviors.unhandled
      }
    }

  def withValues(avg: Double, min: Double, max: Double, events: Long): Behavior[Command] =
    Behaviors.receive {
      case (context, TemperatureReading(value)) =>
        context.log.info(s"Temperature received $value")
        withValues(
          ((avg * events) + value) / (events + 1),
          Math.min(min, value),
          Math.max(max, value),
          events + 1
        )
      case (_, GetMaxTemperature(replyTo)) =>
        replyTo ! max
        Behaviors.same
      case (_, GetMinTemperature(replyTo)) =>
        replyTo ! min
        Behaviors.same
      case (_, GetAverageTemperature(replyTo)) =>
        replyTo ! avg
        Behaviors.same
    }
}
