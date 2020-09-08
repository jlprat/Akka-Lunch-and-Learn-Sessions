package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.{Actor, ActorLogging, Props, Timers}
import akka.pattern.pipe

import scala.concurrent.duration._

import TemperatureGatherer._
import scala.concurrent.{ExecutionContext, Future}

object TemperatureGatherer {
  private case object TimerKey
  sealed trait Command
  case object CheckTemperature extends Command

  def propsSyncTesting(tempChecker: ExecutionContext => Future[Double]): Props =
    Props(new TemperatureGatherer(tempChecker, true))
  def props(tempChecker: ExecutionContext => Future[Double]): Props =
    Props(new TemperatureGatherer(tempChecker, false))
}

class TemperatureGatherer(
    val tempChecker: Function[ExecutionContext, Future[Double]],
    val testMode: Boolean // this flag is only for testing purposes and avoids starting the timers
) extends Actor
    with ActorLogging
    with Timers {

  if (!testMode) timers.startSingleTimer(TimerKey, CheckTemperature, 0.millis)

  val blockingEc = context.system.dispatchers.lookup("blocking-io-dispatcher")

  override def receive: Actor.Receive = {
    case CheckTemperature =>
      val eventualTemp = tempChecker(blockingEc)
      pipe(eventualTemp)(context.dispatcher) to self
      if (!testMode) timers.startSingleTimer(TimerKey, CheckTemperature, 100.millis)
    case temp: Double =>
      if (temp > 50.0) log.info(s"It's too hot here! $temp °C")
      context.parent ! TemperatureStatistics.TemperatureReading(temp)
  }

}
