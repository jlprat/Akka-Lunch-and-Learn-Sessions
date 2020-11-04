package io.github.jlprat.akka.lnl.stash.classic

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.stash.classic.StashBeforeInit._
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem

import com.typesafe.config.ConfigFactory
import akka.testkit.EventFilter

class StashBeforeInitTest
    extends TestKit(ActorSystem("testsystem", ConfigFactory.parseString("""
  akka.loggers = ["akka.testkit.TestEventListener"]
  """)))
    with ImplicitSender
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "StashBeforeInitTest" should "stash messages sent before the initialization one for later processing" in {

    val dropBeforeInitActor = system.actorOf(StashBeforeInit.props())

    EventFilter.info(message = "Stashing request to calculate 3 number of primes", occurrences = 1).intercept {
      dropBeforeInitActor ! Primes(3)
    }

    dropBeforeInitActor ! Initialize

    expectMsg(Processing)
    expectMsg(Done(List(2, 3, 5)))
  }

  it should "process all 'work' messages after the initialization one" in {
    val dropBeforeInitActor = system.actorOf(StashBeforeInit.props())

    dropBeforeInitActor ! Initialize

    dropBeforeInitActor ! Primes(3)

    expectMsg(Processing)
    expectMsg(Done(List(2, 3, 5)))
  }
}
