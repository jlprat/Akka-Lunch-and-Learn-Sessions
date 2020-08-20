package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.Actor


object TemperatureStatistics {
  case class TempetureReading(value: Double)

  case object GetMaxTemperature
  case object GetMinTemperature
  case object GetAverageTemperature
}

/**
  * This class models an actor that receives read temperatures and generates statistics about it
  *
  */
class TemperatureStatistics extends Actor {

  var maxTemp: Double = Double.MinValue
  var minTemp: Double = Double.MaxValue
  var averageTemperature: Double = 0 
  var events: Long = 0
  
  override def receive: Actor.Receive = ???

  
}
