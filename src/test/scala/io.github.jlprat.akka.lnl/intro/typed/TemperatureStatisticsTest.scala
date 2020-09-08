package io.github.jlprat.akka.lnl.intro.typed

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import org.slf4j.event.Level

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.scaladsl.Behaviors

import io.github.jlprat.akka.lnl.intro.typed.TemperatureStatistics.{
  GetAverageTemperature,
  GetMaxTemperature,
  GetMinTemperature,
  TemperatureReading
}
import akka.actor.testkit.typed.Effect.Spawned

class TemperatureStatisticsTest extends AnyFlatSpec with Matchers {

  "TemperatureStatistics" should "fail to get any statistic before getting any reading" in {
    val tempStatsBehavior =
      BehaviorTestKit(
        TemperatureStatistics(true)
      ) // we don't want TemperatureGatherer starting to gather temp and sending it over to the parent

    val probe = TestInbox[Double]()
    tempStatsBehavior.run(GetMaxTemperature(probe.ref))
    probe.hasMessages shouldBe false
    tempStatsBehavior.returnedBehavior shouldBe Behaviors.unhandled

    tempStatsBehavior.run(GetMinTemperature(probe.ref))
    probe.hasMessages shouldBe false
    tempStatsBehavior.returnedBehavior shouldBe Behaviors.unhandled

    tempStatsBehavior.run(GetAverageTemperature(probe.ref))
    probe.hasMessages shouldBe false
    tempStatsBehavior.returnedBehavior shouldBe Behaviors.unhandled
  }

  it should "read temperatures and calculate the right average" in {
    val tempStatsBehavior =
      BehaviorTestKit(
        TemperatureStatistics(true)
      ) // we don't want TemperatureGatherer starting to gather temp and sending it over to the parent

    tempStatsBehavior.run(TemperatureReading(40.1))
    tempStatsBehavior.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "Temperature received 40.1", None, None)
    )

    tempStatsBehavior.clearLog()

    tempStatsBehavior.run(TemperatureReading(34.9))
    tempStatsBehavior.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "Temperature received 34.9", None, None)
    )
  }

  it should "return statistics after reading temperatures" in {
    val tempStatsBehavior = BehaviorTestKit(TemperatureStatistics.withValues(24.4, 21.4, 30.3, 100))
    val probe             = TestInbox[Double]()
    tempStatsBehavior.run(GetMaxTemperature(probe.ref))
    probe.expectMessage(30.3)

    tempStatsBehavior.run(GetMinTemperature(probe.ref))
    probe.expectMessage(21.4)

    tempStatsBehavior.run(GetAverageTemperature(probe.ref))
    probe.expectMessage(24.4)

  }

  it should "start a child on start" in {
    val tempStatsBehavior =
      BehaviorTestKit(
        TemperatureStatistics(true)
      ) // we don't want TemperatureGatherer starting to gather temp and sending it over to the parent
    // tempStatsBehavior.expectEffect(
    //   Spawned(TemperatureGatherer(tempStatsBehavior.ref, Util.getTemperature), "TempGatherer")
    // )
    tempStatsBehavior.expectEffectPF {
      case spawned: Spawned[_] => spawned.childName shouldBe "TempGatherer"
    }
  }

}
