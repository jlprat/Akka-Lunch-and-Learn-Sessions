package io.github.jlprat.akka.lnl.supervision

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.supervision.typed.Watching
import io.github.jlprat.akka.lnl.supervision.typed.Watching._
import org.scalatest.BeforeAndAfterAll
import akka.actor.testkit.typed.scaladsl.ActorTestKit

class WatchingTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll {

  val testKit = ActorTestKit()
  
  override def afterAll(): Unit = testKit.shutdownTestKit()

  "Watching Behavior" should "notify caller when child is finished" in {
    val watchingBehavior = testKit.spawn(Watching.apply())
    val testInbox = testKit.createTestProbe[JobState]()

    watchingBehavior.tell(StartJob("num1", testInbox.ref))

    testInbox.expectMessage(Finished)

  } 
  
}
