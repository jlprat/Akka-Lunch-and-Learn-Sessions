package io.github.jlprat.akka.lnl.intro.classic

import org.scalatest.matchers.should.Matchers
import akka.testkit.TestActorRef
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMaxTemperature
import org.scalatest.flatspec.AnyFlatSpecLike
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.pattern.ask
import org.scalatest.concurrent.ScalaFutures
import scala.concurrent.duration._
import akka.util.Timeout
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.time.Millis
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetMinTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetAverageTemperature
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.TemperatureReading

/**
  * Unit Test approach to actors. Tends to result on whitebox testing
  *
  * This synchronous tests are mostly discouraged as might fail to work with advanced features:
  * Due to the synchronous nature of TestActorRef it will not work with some support traits that Akka provides as they require asynchronous behaviors to function properly. Examples of traits that do not mix well with test actor refs are PersistentActor and AtLeastOnceDelivery provided by Akka Persistence.
  */
class TemperatureStatisticsTest
    extends TestKit(ActorSystem())
    with AnyFlatSpecLike
    with Matchers
    with ScalaFutures {

  implicit val timeout: Timeout = 300.millis
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(5, Millis))

  "TemperatureStatistics" should "fail to get any statistic before getting any reading" in {

    val temperatureStatistics = TestActorRef[TemperatureStatistics]
    val futureMaxTemp         = temperatureStatistics ? GetMaxTemperature
    futureMaxTemp.failed.futureValue.isInstanceOf[IllegalStateException]

    val futureMinTemp = temperatureStatistics ? GetMinTemperature
    futureMinTemp.failed.futureValue.isInstanceOf[IllegalStateException]

    val futureAverageTemp = temperatureStatistics ? GetAverageTemperature
    futureAverageTemp.failed.futureValue.isInstanceOf[IllegalStateException]
  }

  it should "read temperatures" in {
    val temperatureStatistics = TestActorRef[TemperatureStatistics]
    temperatureStatistics ! TemperatureReading(40.1)

    temperatureStatistics.underlyingActor.events shouldBe 1
    temperatureStatistics.underlyingActor.averageTemperature shouldBe 40.1
    temperatureStatistics.underlyingActor.minTemp shouldBe 40.1
    temperatureStatistics.underlyingActor.maxTemp shouldBe 40.1

    temperatureStatistics ! TemperatureReading(34.9)

    temperatureStatistics.underlyingActor.events shouldBe 2
    temperatureStatistics.underlyingActor.averageTemperature shouldBe 37.5
    temperatureStatistics.underlyingActor.maxTemp shouldBe 40.1
    temperatureStatistics.underlyingActor.minTemp shouldBe 34.9

  }

  it should "return statistics after reading temperatures" in {
    val temperatureStatistics = TestActorRef[TemperatureStatistics]
    temperatureStatistics ! TemperatureReading(40.1)
    temperatureStatistics ! TemperatureReading(34.9)

    val futureMaxTemp = temperatureStatistics ? GetMaxTemperature
    futureMaxTemp.futureValue shouldBe 40.1

    val futureMinTemp = temperatureStatistics ? GetMinTemperature
    futureMinTemp.futureValue shouldBe 34.9

    val futureAverageTemp = temperatureStatistics ? GetAverageTemperature
    futureAverageTemp.futureValue shouldBe 37.5

  }

}
