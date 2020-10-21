package io.github.jlprat.akka.lnl.supervision

import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpecLike
import io.github.jlprat.akka.lnl.supervision.typed.Shutdown
import io.github.jlprat.akka.lnl.supervision.typed.Shutdown.GracefulShutdown
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LoggingTestKit
import io.github.jlprat.akka.lnl.supervision.typed.Shutdown.Init

class ShutdownTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  val shutdownBehavior = testKit.spawn(Shutdown())
  shutdownBehavior.tell(Init)

  "Shutdown Behavior" should "log clean up messages for itself and its child" in {
    LoggingTestKit.info("Cleaning Up Tasks").withOccurrences(3).expect {
      shutdownBehavior.tell(GracefulShutdown)
    }
  }
}
