package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.stash.typed.StashBeforeInit._
import akka.actor.testkit.typed.scaladsl.LoggingTestKit

class StashBeforeInitTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  "StashBeforeInit" should "stash messages sent before the initialization one for later processing" in {

    val dropBeforeInitBehavior = testKit.spawn(StashBeforeInit())

    val probe = testKit.createTestProbe[Status]()

    LoggingTestKit.info("Stashing request to calculate 3 primes").expect {
      dropBeforeInitBehavior.tell(Primes(3, probe.ref))
    }

    dropBeforeInitBehavior.tell(Initialize)
    probe.expectMessage(Processing)
    probe.expectMessage(Done(List(2, 3, 5)))
  }

  it should "process all 'work' messages after the initialization one" in {
    val initializedBehavior = testKit.spawn(StashBeforeInit.initialized())

    val probe = testKit.createTestProbe[Status]()
    initializedBehavior.tell(Primes(3, probe.ref))

    probe.expectMessage(Processing)
    probe.expectMessage(Done(List(2, 3, 5)))
  }
}
