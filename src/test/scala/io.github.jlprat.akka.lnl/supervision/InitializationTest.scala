package io.github.jlprat.akka.lnl.supervision

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit

import org.slf4j.event.Level

import io.github.jlprat.akka.lnl.supervision.typed.Initialization
import io.github.jlprat.akka.lnl.supervision.typed.Initialization.Init

class InitializationTest extends AnyFlatSpec with Matchers {
  "Initialization Example variant 1" should "initialize the DB on start" in {
    val initializationHookBehavior = BehaviorTestKit(Initialization.apply())
    initializationHookBehavior.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "DB initialized", None, None)
    )
    initializationHookBehavior.clearLog()
    
    initializationHookBehavior.run(Init)
    initializationHookBehavior.logEntries().size shouldBe 0
  }

  "Initialization Example variant 2" should "initialize the DB on demand" in {
    val initializationHookBehavior = BehaviorTestKit(Initialization.withInitMessage())
    initializationHookBehavior.logEntries().size shouldBe 0

    initializationHookBehavior.run(Init)
    initializationHookBehavior.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "DB initialized", None, None)
    )
  }
}
