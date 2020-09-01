package io.github.jlprat.akka.lnl.intro.typed

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import io.github.jlprat.akka.lnl.intro.typed.TemperatureStatistics.TemperatureReading

object TemperatureGatherer {
  sealed trait Command
  case object CheckTemperature                            extends Command
  private case class ReadTemperature(temperature: Double) extends Command

  def apply(
      parent: ActorRef[TemperatureStatistics.Command],
      obtainTemp: ExecutionContext => Future[Double]
  ): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val ec: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.blocking())

      Behaviors.withTimers { timers =>
        timers.startSingleTimer(CheckTemperature, 10.millis)
        Behaviors.receiveMessage {
          case CheckTemperature =>
            context.pipeToSelf(obtainTemp(ec)) {
              case Success(temp) => ReadTemperature(temp)
              case Failure(ex)   => throw ex
            }
            timers.startSingleTimer(CheckTemperature, 100.millis)
            Behaviors.same
          case ReadTemperature(temp) =>
            if (temp > 50.0) context.log.info(s"It's too hot here! $temp °C")
            parent ! TemperatureReading(temp)
            Behaviors.same
        }
      }
    }
}