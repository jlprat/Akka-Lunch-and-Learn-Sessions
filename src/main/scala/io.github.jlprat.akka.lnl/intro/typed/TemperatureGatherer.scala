package io.github.jlprat.akka.lnl.intro.typed

import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors

import java.io.{BufferedReader, InputStreamReader}

import scala.concurrent.{blocking, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import io.github.jlprat.akka.lnl.intro.typed.TemperatureStatistics.TemperatureReading

object TemperatureGatherer {
  sealed trait Command
  case object CheckTemperature                            extends Command
  private case class ReadTemperature(temperature: Double) extends Command

  def apply(parent: ActorRef[TemperatureStatistics.Command], obtainTemp: ExecutionContext => Future[Double]): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val ec: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.fromConfig("my-blocking-dispatcher"))

      context.scheduleOnce(0.millis, context.self, CheckTemperature)

      Behaviors.receiveMessage {
        case CheckTemperature =>
          context.pipeToSelf(obtainTemp(ec)) {
            case Success(temp) => ReadTemperature(temp)
            case Failure(ex)   => throw ex
          }
          context.scheduleOnce(100.millis, context.self, CheckTemperature)
          Behaviors.same
        case ReadTemperature(temp) =>
          if (temp > 42.0) context.log.info(s"It's too hot here! $temp °C")
          parent ! TemperatureReading(temp)
          Behaviors.same
      }
    }

  private[typed] def getTemperature()(implicit ec: ExecutionContext): Future[Double] =
    Future {
      blocking {
        val reader = new BufferedReader(
          new InputStreamReader(
            Runtime.getRuntime().exec("acpi -t | cut -d ',' -f2").getInputStream()
          )
        )
        reader.readLine().toDouble
      }
    }
}
