package io.github.jlprat.akka.lnl.persistence.typed

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.persistence.typed.PersistentKeyValueStoreWithSnapshots._
import java.{util => ju}
import akka.actor.testkit.typed.scaladsl.LoggingTestKit

class PersistentKeyValueStoreWithSnapshotsTest
    extends ScalaTestWithActorTestKit(s"""
      akka.actor.allow-java-serialization = "true"
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${ju.UUID.randomUUID().toString}"
    """)
    with AnyFlatSpecLike
    with Matchers {

  "PersistentKeyValueStoreWithSnapshots" should "save key-value pairs" in {
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-1"))
    val probePut      = testKit.createTestProbe[PutResponse]()
    val probeGet      = testKit.createTestProbe[GetResponse]()

    persistentKVS.tell(Put("city", "Berlin", probePut.ref))
    probePut.expectMessage(Stored("city"))

    persistentKVS.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }

  it should "not persist the key if it exceeds the maximum of 100 chars" in {
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-2"))
    val probePut      = testKit.createTestProbe[PutResponse]()

    val longKey = List.fill(101)("a").mkString
    persistentKVS.tell(Put(longKey, "Berlin", probePut.ref))
    probePut.expectMessage(
      Failed(longKey, "Either key or value exceed maximum size")
    )
  }

  it should "not persist the value if it exceeds the maximum of 100 chars" in {
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-2"))
    val probePut      = testKit.createTestProbe[PutResponse]()

    persistentKVS.tell(Put("city", List.fill(501)("a").mkString, probePut.ref))
    probePut.expectMessage(Failed("city", "Either key or value exceed maximum size"))
  }

  it should "return missing when looking up for a missing key" in {
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-3"))
    val probeGet      = testKit.createTestProbe[GetResponse]()

    persistentKVS.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Missing("city"))

  }

  it should "process commands one after the other when being persisted" in {
    // When persisting events with persist or persistAll it is guaranteed that the EventSourcedBehavior
    // will not receive further commands until after the events have been confirmed to be persisted and
    // additional side effects have been run. Incoming messages are stashed automatically until the
    // persist is completed.
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-4"))
    val probePut      = testKit.createTestProbe[PutResponse]()
    val probeGet      = testKit.createTestProbe[GetResponse]()

    persistentKVS.tell(Put("city", "Berlin", probePut.ref))
    persistentKVS.tell(Get("city", probeGet.ref))

    probeGet.expectMessage(Retrieved("Berlin"))
    probePut.expectMessage(Stored("city"))

    persistentKVS.tell(Get("city", probeGet.ref))
    probeGet.expectMessage(Retrieved("Berlin"))
  }

  it should "keep its state between restarts" in {
    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-5"))
    val probePut      = testKit.createTestProbe[PutResponse]()
    val probeGet      = testKit.createTestProbe[GetResponse]()

    persistentKVS.tell(Put("city", "Berlin", probePut.ref))
    persistentKVS.tell(Put("street", "Main", probePut.ref))
    persistentKVS.tell(Put("job", "Not Disclosed", probePut.ref))

    probePut.expectMessage(Stored("city"))
    probePut.expectMessage(Stored("street"))
    probePut.expectMessage(Stored("job"))

    testKit.stop(persistentKVS)

    LoggingTestKit.debug("Replaying events: from: 1").expect {
      val restartedKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-5"))
      restartedKVS.tell(Get("city", probeGet.ref))
      restartedKVS.tell(Get("street", probeGet.ref))
      restartedKVS.tell(Get("job", probeGet.ref))
      probeGet.expectMessage(Retrieved("Berlin"))
      probeGet.expectMessage(Retrieved("Main"))
      probeGet.expectMessage(Retrieved("Not Disclosed"))
    }
  }

  it should "create snapshots after 100 commands" in {

    val persistentKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-6"))
    val probePut      = testKit.createTestProbe[PutResponse]()
    val probeGet      = testKit.createTestProbe[GetResponse]()

    (1 to 100).foreach { i =>
      persistentKVS.tell(Put("city", s"Berlin-$i", probePut.ref))
      probePut.expectMessage(Stored("city"))

      persistentKVS.tell(Get("city", probeGet.ref))
      probeGet.expectMessage(Retrieved(s"Berlin-$i"))
    }

    testKit.stop(persistentKVS)

    LoggingTestKit.debug("Replaying events: from: 101").expect {
      val restartedKVS = testKit.spawn(PersistentKeyValueStoreWithSnapshots("kvs-6"))
      restartedKVS.tell(Get("city", probeGet.ref))
      probeGet.expectMessage(Retrieved("Berlin-100"))
    }
  }
}
