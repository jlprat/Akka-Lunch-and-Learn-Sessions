package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.stash.typed.DropBeforeInit.Primes
import io.github.jlprat.akka.lnl.stash.typed.DropBeforeInit.Status
import io.github.jlprat.akka.lnl.stash.typed.DropBeforeInit.Discarded
import io.github.jlprat.akka.lnl.stash.typed.DropBeforeInit.Done
import io.github.jlprat.akka.lnl.stash.typed.DropBeforeInit.Processing

class DropBeforeInitTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {
  
  "DropBeforeInitTest" should "drop 'work' messages sent before the initialization one" in {
      val dropBeforeInitBehavior = testKit.spawn(DropBeforeInit())

      val probe = testKit.createTestProbe[Status]()
      dropBeforeInitBehavior.tell(Primes(3, probe.ref))
      probe.expectMessage(Discarded)

      dropBeforeInitBehavior.tell(Primes(66, probe.ref))
      probe.expectMessage(Discarded)

  }

  it should "process all 'work' messages after the initialization one" in {
    val initializedBehavior = testKit.spawn(DropBeforeInit.initialized())

    val probe = testKit.createTestProbe[Status]()
    initializedBehavior.tell(Primes(3, probe.ref))

    probe.expectMessage(Processing)
    probe.expectMessage(Done(List(2,3,5)))

  }
}
