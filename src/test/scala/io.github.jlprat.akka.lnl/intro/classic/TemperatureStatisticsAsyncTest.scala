package io.github.jlprat.akka.lnl.intro.classic

import org.scalatest.matchers.should.Matchers
import akka.testkit.TestActorRef
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMaxTemperature
import org.scalatest.flatspec.AnyFlatSpecLike
import akka.testkit.TestKit
import akka.actor.ActorSystem
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMinTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetAverageTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.TemperatureReading
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import scala.concurrent.duration._

/**
  * 
  */
class TemperatureStatisticsAsyncTest extends TestKit(ActorSystem()) with ImplicitSender with AnyFlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "TemperatureStatistics Async" should "fail to get any statistic before getting any reading" in {
    
    val temperatureStatistics = system.actorOf(TemperatureStatistics.props())
    
    temperatureStatistics ! GetMaxTemperature
    expectNoMessage(200.millis)

    temperatureStatistics ! GetMinTemperature
    expectNoMessage(200.millis)

    temperatureStatistics ! GetAverageTemperature
    expectNoMessage(200.millis)
  }

  it should "return statistics after reading temperatures" in {
    val temperatureStatistics = TestActorRef[TemperatureStatistics]
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
