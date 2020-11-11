package io.github.jlprat.akka.lnl.intro.classic

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class TemperatureGathererAsyncTest
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  @nowarn
  private def fakeSysCallToCheckTemp(ec: ExecutionContext): Future[Double] = {
    Future.successful(32.3)
  }

  "TemperatureGatherer" should "start checking the temperature" in {
    val parent = TestProbe()
    parent.childActorOf(TemperatureGatherer.props(fakeSysCallToCheckTemp))
    parent.expectMsg(TemperatureStatistics.TemperatureReading(32.3))
  }

  it should "start check the temperature again in 100 ms" in {
    val parent = TestProbe()
    parent.childActorOf(TemperatureGatherer.props(fakeSysCallToCheckTemp))
    parent.expectMsg(TemperatureStatistics.TemperatureReading(32.3))
    parent.expectNoMessage(90.millis)
    parent.expectMsg(30.millis, TemperatureStatistics.TemperatureReading(32.3))
  }

}
