package io.github.jlprat.akka.lnl.supervision

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.scaladsl.BehaviorTestKit

import org.slf4j.event.Level

import io.github.jlprat.akka.lnl.supervision.typed.Initialization
import io.github.jlprat.akka.lnl.supervision.typed.Initialization.Init
import io.github.jlprat.akka.lnl.supervision.typed.Restart
import akka.actor.testkit.typed.Effect.Spawned
import io.github.jlprat.akka.lnl.supervision.typed.Restart.Boom
import akka.actor.testkit.typed.Effect.Stopped
import akka.actor.testkit.typed.Effect.Watched
import akka.actor.typed.ActorRef
import io.github.jlprat.akka.lnl.supervision.typed.Restart.ChildCommand
import io.github.jlprat.akka.lnl.supervision.typed.Restart.DoThings

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
    val initializationMessageBehavior = BehaviorTestKit(Initialization.withInitMessage())
    initializationMessageBehavior.logEntries().size shouldBe 0

    initializationMessageBehavior.run(Init)
    initializationMessageBehavior.logEntries() shouldBe Seq(
      CapturedLogEvent(Level.INFO, "DB initialized", None, None)
    )
  }

  "Restart Example variant 1" should "start child only on start and not on restart" in {
    val childOnStart = BehaviorTestKit(Restart.apply())
    childOnStart.expectEffect(Spawned(Restart.child,"child"))
    
    childOnStart.run(Boom)
    childOnStart.hasEffects() shouldBe false
  }

  "Restart Example variant 2" should "start child on every restart" in {
    val childOnRestart = BehaviorTestKit(Restart.recreateChildOnRestart())
    childOnRestart.expectEffect(Spawned(Restart.child,"child"))
    
    childOnRestart.run(Boom)
    //println(childOnRestart.retrieveAllEffects())
    childOnRestart.expectEffectType[Watched[ActorRef[ChildCommand]]]
    childOnRestart.expectEffect(Stopped("child"))
    childOnRestart.expectEffect(Spawned(Restart.child,"child"))
  }
}
