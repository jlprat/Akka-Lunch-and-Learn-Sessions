package io.github.jlprat.akka.lnl.supervision

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpecLike
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit

import io.github.jlprat.akka.lnl.supervision.typed.WatchingAlt
import io.github.jlprat.akka.lnl.supervision.typed.WatchingAlt._

class WatchingAltTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  "Watching Behavior" should "notify caller when child is finished" in {
    val watchingBehavior = testKit.spawn(WatchingAlt.apply())
    val testInbox        = testKit.createTestProbe[JobState]()

    watchingBehavior.tell(StartJob("num1", testInbox.ref))

    testInbox.expectMessage(Finished)

  }

}
