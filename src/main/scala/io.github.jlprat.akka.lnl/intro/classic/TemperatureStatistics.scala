package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.{Actor, ActorLogging, Props}

import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics._

object TemperatureStatistics {
  case class TemperatureReading(value: Double)

  case object GetMaxTemperature
  case object GetMinTemperature
  case object GetAverageTemperature

  def props(): Props = Props(classOf[TemperatureStatistics])
}

/**
  * This class models an actor that receives read temperatures and generates statistics about it
  */
class TemperatureStatistics extends Actor with ActorLogging {

  var maxTemp: Double            = Double.MinValue
  var minTemp: Double            = Double.MaxValue
  var averageTemperature: Double = 0
  var events: Long               = 0

  /**
    * Default receive handler. Until the first measure is not received it doesn't reply    *
    */
  override def receive: Actor.Receive = {
    case TemperatureReading(value) =>
      log.info(s"received temp: $value")
      events = 1
      maxTemp = Math.max(maxTemp, value)
      minTemp = Math.min(minTemp, value)
      averageTemperature = value
      context.become(withData)
    case _ =>
      log.info("No Data")
      throw new IllegalStateException("No Temperature Data")
  }

  /**
    * Receive handler for when there is already data to perform statistics
    */
  private def withData: Actor.Receive = {
    case TemperatureReading(value) =>
      averageTemperature = ((averageTemperature * events) + value) / (events + 1)
      events = events + 1
      maxTemp = Math.max(maxTemp, value)
      minTemp = Math.min(minTemp, value)
    case GetMaxTemperature     => sender() ! maxTemp
    case GetMinTemperature     => sender() ! minTemp
    case GetAverageTemperature => sender() ! averageTemperature
  }

}