package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.github.jlprat.akka.lnl.stash.typed.SimpleKeyValueStore._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class SimpleKeyValueStoreTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  "SimpleKeyValueStore" should "save key-value pairs" in {
    val simpleKeyValueStoreBehavior = testKit.spawn(SimpleKeyValueStore())
    val probePut                    = testKit.createTestProbe[PutResponse]()
    val probeGet                    = testKit.createTestProbe[GetResponse]()

    simpleKeyValueStoreBehavior.tell(Put("city", "Berlin", probePut.ref))
    probePut.expectMessage(Stored("city"))

    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }

  it should "be eventually consistent" in {
    val simpleKeyValueStoreBehavior = testKit.spawn(SimpleKeyValueStore())
    val probePut                    = testKit.createTestProbe[PutResponse]()
    val probeGet                    = testKit.createTestProbe[GetResponse]()

    simpleKeyValueStoreBehavior.tell(Put("city", "Berlin", probePut.ref))
    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))

    probeGet.expectMessage(Missing("city"))
    probePut.expectMessage(Stored("city"))

    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }
}
