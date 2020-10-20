package io.github.jlprat.akka.lnl.supervision

import akka.actor.testkit.typed.CapturedLogEvent
import akka.actor.testkit.typed.Effect.Watched
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, LoggingTestKit, ScalaTestWithActorTestKit, Effects}

import akka.actor.typed.ActorRef

import io.github.jlprat.akka.lnl.supervision.typed.Initialization
import io.github.jlprat.akka.lnl.supervision.typed.Initialization.Init

import io.github.jlprat.akka.lnl.supervision.typed.Restart
import io.github.jlprat.akka.lnl.supervision.typed.Restart.{Boom, ChildCommand, DoThings}

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import org.slf4j.event.Level

class InitializationTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

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
    childOnStart.expectEffect(Effects.spawned(Restart.child, "child"))

    childOnStart.run(Boom)
    childOnStart.hasEffects() shouldBe false
  }

  "Restart Example variant 2" should "start child on every restart" in {
    val childOnRestart = BehaviorTestKit(Restart.recreateChildOnRestart())
    childOnRestart.expectEffect(Effects.spawned(Restart.child, "child"))

    childOnRestart.run(Boom)
    childOnRestart.expectEffectType[Watched[ActorRef[ChildCommand]]]
    childOnRestart.expectEffect(Effects.stopped("child"))
    //childOnRestart.expectEffect(Effects.spawned(Restart.child,"child")) //FIXME this fails!
  }

  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Restart Example variant 1 async" should "start child only on start and not on restart" in {
    val childOnStart = testKit.spawn(Restart.apply())
    LoggingTestKit.info("creating child").withOccurrences(0).expect {
      childOnStart.tell(Boom)
    }
    childOnStart.tell(DoThings)
  }

  "Restart Example variant 2 async" should "start child on start and restart" in {
    val childOnStart = testKit.spawn(Restart.recreateChildOnRestart())
    LoggingTestKit.info("creating child").expect {
      childOnStart.tell(Boom)
    }

  }

}
