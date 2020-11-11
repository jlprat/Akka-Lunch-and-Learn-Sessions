package io.github.jlprat.akka.lnl.intro.util

import scala.concurrent.ExecutionContext.Implicits.global

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class TempTest extends AnyFlatSpec with Matchers with ScalaFutures {
  
  "getTempFromShell" should "return a double between 20 and 100 degrees" in {
    val temp = Util.getTemperature(global).futureValue

    temp should be > 20.0
    temp should be < 100.0
  }
}
