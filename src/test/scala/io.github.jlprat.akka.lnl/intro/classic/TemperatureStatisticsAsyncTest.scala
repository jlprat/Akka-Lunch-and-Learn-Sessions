package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._

import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.{
  GetAverageTemperature,
  GetMaxTemperature,
  GetMinTemperature,
  TemperatureReading
}

/**
  */
class TemperatureStatisticsAsyncTest
    extends TestKit(ActorSystem())
    with ImplicitSender
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "TemperatureStatistics Async" should "fail to get any statistic before getting any reading" in {

    val temperatureStatistics = system.actorOf(TemperatureStatistics.propsSyncTesting())

    temperatureStatistics ! GetMaxTemperature
    expectNoMessage(100.millis)

    temperatureStatistics ! GetMinTemperature
    expectNoMessage(100.millis)

    temperatureStatistics ! GetAverageTemperature
    expectNoMessage(100.millis)
  }

  it should "return statistics after reading temperatures" in {
    val temperatureStatistics = system.actorOf(TemperatureStatistics.propsSyncTesting())
    temperatureStatistics ! TemperatureReading(40.1)
    temperatureStatistics ! TemperatureReading(34.9)

    temperatureStatistics ! GetMaxTemperature
    expectMsg(40.1)

    temperatureStatistics ! GetMinTemperature
    expectMsg(34.9)

    temperatureStatistics ! GetAverageTemperature
    expectMsg(37.5)
  }

}
