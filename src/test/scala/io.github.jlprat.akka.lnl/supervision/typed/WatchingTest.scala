package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.github.jlprat.akka.lnl.supervision.typed.Watching
import io.github.jlprat.akka.lnl.supervision.typed.Watching._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class WatchingTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  "Watching Behavior" should "notify caller when child is finished" in {
    val watchingBehavior = testKit.spawn(Watching.apply())
    val testInbox        = testKit.createTestProbe[JobState]()

    watchingBehavior.tell(StartJob("num1", testInbox.ref))

    testInbox.expectMessage(Finished)

  }

}
