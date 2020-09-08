package io.github.jlprat.akka.lnl.intro.typed

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import akka.actor.testkit.typed.scaladsl.ActorTestKit

import scala.concurrent.duration._

import io.github.jlprat.akka.lnl.intro.typed.TemperatureStatistics.{
  GetAverageTemperature,
  GetMaxTemperature,
  GetMinTemperature,
  TemperatureReading
}

class TemperatureStatisticsAsyncTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val testKit = ActorTestKit()

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "TemperatureStatistics" should "fail to get any statistic before getting any reading" in {
    val tempStatsBehavior = testKit.spawn(TemperatureStatistics(true))

    val probe = testKit.createTestProbe[Double]()
    tempStatsBehavior ! GetMaxTemperature(probe.ref)
    probe.expectNoMessage(100.millis)

    tempStatsBehavior ! GetMinTemperature(probe.ref)
    probe.expectNoMessage(100.millis)

    tempStatsBehavior ! GetAverageTemperature(probe.ref)
    probe.expectNoMessage(100.millis)
  }

  it should "return statistics after reading temperatures" in {
    val tempStatsBehavior = testKit.spawn(TemperatureStatistics(true))

    val probe = testKit.createTestProbe[Double]()

    tempStatsBehavior ! TemperatureReading(40.1)
    tempStatsBehavior ! TemperatureReading(34.9)

    tempStatsBehavior ! GetAverageTemperature(probe.ref)
    probe.expectMessage(37.5)
    tempStatsBehavior ! GetMinTemperature(probe.ref)
    probe.expectMessage(34.9)
    tempStatsBehavior ! GetMaxTemperature(probe.ref)
    probe.expectMessage(40.1)
  }

}
