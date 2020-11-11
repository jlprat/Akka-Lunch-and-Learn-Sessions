package io.github.jlprat.akka.lnl.persistence.classic

import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import io.github.jlprat.akka.lnl.persistence.classic.PersistentKeyValueStoreWithSnapshot._
import com.typesafe.config.ConfigFactory
import java.{util => ju}
import akka.testkit.EventFilter

class PersistentKeyValueStoreWithSnapshotTest
    extends TestKit(
      ActorSystem(
        "testsystem",
        ConfigFactory
          .parseString(s"""akka.persistence.snapshot-store.local.dir = "target/snapshot-${ju.UUID
            .randomUUID()
            .toString}"
              akka.loggers = ["akka.testkit.TestEventListener"]""")
          .withFallback(ConfigFactory.defaultApplication())
      )
    )
    with ImplicitSender
    with AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll {

  "PersistentKeyValueStoreWithSnapshot" should "save key-value pairs" in {
    val persistentKVS = system.actorOf(PersistentKeyValueStoreWithSnapshot.props(("kvs-1")))

    persistentKVS ! Put("city", "Berlin")
    expectMsg(Persisted("city"))

    persistentKVS ! Get("city")
    expectMsg(Some("Berlin"))
  }

  it should "not persist the key if it exceeds the maximum of 100 chars" in {
    val persistentKVS = system.actorOf(PersistentKeyValueStoreWithSnapshot.props(("kvs-2")))

    val longKey = List.fill(101)("a").mkString
    persistentKVS ! Put(longKey, "Berlin")
    expectMsg(Failure("Either key or value exceed maximum size"))
  }

  it should "not persist the value if it exceeds the maximum of 500 chars" in {
    val persistentKVS = system.actorOf(PersistentKeyValueStoreWithSnapshot.props(("kvs-3")))

    val longKey = List.fill(501)("a").mkString
    persistentKVS ! Put("city", longKey)
    expectMsg(Failure("Either key or value exceed maximum size"))
  }

  it should "keep state between restarts" in {
    val persistentKVS = system.actorOf(PersistentKeyValueStoreWithSnapshot.props(("kvs-4")))

    persistentKVS ! Put("city", "Berlin")
    expectMsg(Persisted("city"))

    persistentKVS ! Get("city")
    expectMsg(Some("Berlin"))

    persistentKVS ! BOOM

    persistentKVS ! Get("city")
    expectMsg(Some("Berlin"))

  }

  it should "keep state between restarts using snapshots" in {
    val persistentKVS = system.actorOf(PersistentKeyValueStoreWithSnapshot.props(("kvs-5")))

    (1 to 102).foreach { i =>
      persistentKVS ! Put("city", s"Berlin-$i")
      expectMsg(Persisted("city"))
    }

    persistentKVS ! Get("city")
    expectMsg(Some("Berlin-102"))

    Thread.sleep(500)

    EventFilter.info(message = "Snapshot restored", occurrences = 1).intercept {
      EventFilter.info(message = "event restored", occurrences = 2).intercept {
        persistentKVS ! BOOM

        persistentKVS ! Get("city")
        expectMsg(Some("Berlin-102"))
      }
    }
  }
}
