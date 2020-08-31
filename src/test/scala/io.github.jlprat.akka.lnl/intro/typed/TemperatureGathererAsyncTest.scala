package io.github.jlprat.akka.lnl.intro.typed

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.{
  BehaviorTestKit,
  ManualTime,
  ScalaTestWithActorTestKit,
  TestProbe
}

import org.scalatest.flatspec.AnyFlatSpecLike

import scala.annotation.nowarn
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

import org.slf4j.event.Level

class TemperatureGathererAsyncTest
    extends ScalaTestWithActorTestKit(ManualTime.config)
    with AnyFlatSpecLike {

  val manualTime: ManualTime = ManualTime()

  @nowarn
  private def fakeSysCallToCheckHotTemp(ec: ExecutionContext): Future[Double] = {
    Future.successful(42.3)
  }

  @nowarn
  private def fakeSysCallToCheckTemp(ec: ExecutionContext): Future[Double] = {
    Future.successful(32.3)
  }

  "TemperatureGatherer" should "start checking the temperature" in {
    val fakeFather = TestProbe[TemperatureStatistics.Command]()
    spawn(TemperatureGatherer(fakeFather.ref, fakeSysCallToCheckTemp))
    manualTime.expectNoMessageFor(10.millis, fakeFather)
    fakeFather.expectMessage(TemperatureStatistics.TemperatureReading(32.3))
  }

  it should "check the temperature every 100 millis" in {
    val fakeFather = TestProbe[TemperatureStatistics.Command]()
    spawn(TemperatureGatherer(fakeFather.ref, fakeSysCallToCheckTemp))

    manualTime.expectNoMessageFor(10.millis)
    fakeFather.expectMessage(TemperatureStatistics.TemperatureReading(32.3))

    manualTime.expectNoMessageFor(80.millis) // 10 + 80
    fakeFather.expectNoMessage(Duration.Zero)

    manualTime.timePasses(20.millis) // 10 + 80 + 20
    fakeFather.expectMessage(TemperatureStatistics.TemperatureReading(32.3))
  }

  ignore should "log if too hot" in {
    val fakeFather = TestProbe[TemperatureStatistics.Command]()
    val temperatureGatherer =
      BehaviorTestKit(TemperatureGatherer(fakeFather.ref, fakeSysCallToCheckHotTemp))
    temperatureGatherer.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "It's too hot here! 42.3 °C", None, None)
    )
  }
}
