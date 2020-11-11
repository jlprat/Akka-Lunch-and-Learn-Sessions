package io.github.jlprat.akka.lnl.stash.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.github.jlprat.akka.lnl.stash.typed.TransactionalKeyValueStore._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class TransactionalKeyValueStoreTest
    extends ScalaTestWithActorTestKit
    with AnyFlatSpecLike
    with Matchers {

  "TransactionalKeyValueStore" should "save key-value pairs" in {
    val simpleKeyValueStoreBehavior = testKit.spawn(TransactionalKeyValueStore())
    val probePut                    = testKit.createTestProbe[PutResponse]()
    val probeGet                    = testKit.createTestProbe[GetResponse]()

    simpleKeyValueStoreBehavior.tell(Put("city", "Berlin", probePut.ref))
    probePut.expectMessage(Stored("city"))

    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }

  it should "behave transactional-like" in {
    val simpleKeyValueStoreBehavior = testKit.spawn(TransactionalKeyValueStore())
    val probePut                    = testKit.createTestProbe[PutResponse]()
    val probeGet                    = testKit.createTestProbe[GetResponse]()

    simpleKeyValueStoreBehavior.tell(Put("city", "Berlin", probePut.ref))
    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))

    probeGet.expectMessage(Retrieved("Berlin"))
    probePut.expectMessage(Stored("city"))

    simpleKeyValueStoreBehavior.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }
}
