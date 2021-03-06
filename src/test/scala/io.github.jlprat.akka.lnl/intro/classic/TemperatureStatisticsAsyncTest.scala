package io.github.jlprat.akka.lnl.intro.classic

import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetAverageTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMaxTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMinTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.TemperatureReading
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

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

    val temperatureStatistics =
      system.actorOf(
        TemperatureStatistics.propsSyncTesting()
      ) // we don't want TemperatureGatherer starting to gather temp and sending it over to the parent

    temperatureStatistics ! GetMaxTemperature
    expectNoMessage(100.millis)

    temperatureStatistics ! GetMinTemperature
    expectNoMessage(100.millis)

    temperatureStatistics ! GetAverageTemperature
    expectNoMessage(100.millis)
  }

  it should "return statistics after reading temperatures" in {
    val temperatureStatistics =
      system.actorOf(
        TemperatureStatistics.propsSyncTesting()
      ) // we don't want TemperatureGatherer starting to gather temp and sending it over to the parent
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
