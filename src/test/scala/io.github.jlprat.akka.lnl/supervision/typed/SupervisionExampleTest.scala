package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.testkit.typed.scaladsl.{
  BehaviorTestKit,
  LoggingTestKit,
  ScalaTestWithActorTestKit
}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import akka.actor.testkit.typed.Effect.Spawned
import io.github.jlprat.akka.lnl.supervision.typed.SupervisionExample.{
  Init,
  Key,
  Product => MyProduct,
  Save,
  Stored,
  Retrieve
}

class SupervisionExampleTest extends ScalaTestWithActorTestKit with AnyFlatSpecLike with Matchers {

  "SupervisionExample" should "spawn a child on initialization" in {
    val supervisionBehavior = BehaviorTestKit(SupervisionExample())
    supervisionBehavior.run(Init)
    supervisionBehavior.expectEffectPF {
      case eff @ Spawned(_, "store", _) => eff
    }
  }

  it should "save and retrieve elements" in {
    val supervisionExample = testKit.spawn(SupervisionExample())
    supervisionExample.tell(Init)

    val keyClient = testKit.createTestProbe[Key]()
    supervisionExample.tell(Save("key1", 3, keyClient.ref))
    val expectedKey = Key("key1".hashCode().toString())
    keyClient.expectMessage(expectedKey)

    val storedClient = testKit.createTestProbe[Stored]()
    supervisionExample.tell(Retrieve(expectedKey, storedClient.ref))
    val expectedValue = Stored(MyProduct(expectedKey.id, "key1", 3))
    storedClient.expectMessage(expectedValue)
  }

  it should "fail when trying to retrieve a key that doesn't exist but be restarted" in {
    val supervisionExample = testKit.spawn(SupervisionExample())
    supervisionExample.tell(Init)

    val nonExistingKey = "DOESN'T EXIST"
    val storedClient   = testKit.createTestProbe[Stored]()

    LoggingTestKit.error("I might lost my state").expect {
      supervisionExample.tell(Retrieve(Key(nonExistingKey), storedClient.ref))
    }

    storedClient.expectNoMessage()

    val keyClient = testKit.createTestProbe[Key]()
    supervisionExample.tell(Save("key1", 3, keyClient.ref))
    val expectedKey = Key("key1".hashCode().toString())
    keyClient.expectMessage(expectedKey)
  }

  it should "fail for good when 3 errors happen within a second" in {
    val supervisionExample = testKit.spawn(SupervisionExample())
    supervisionExample.tell(Init)

    val nonExistingKey = "DOESN'T EXIST"
    val storedClient   = testKit.createTestProbe[Stored]()

    LoggingTestKit.error("I might lost my state").expect {
      supervisionExample.tell(Retrieve(Key(nonExistingKey), storedClient.ref))
    }
    LoggingTestKit.error("I might lost my state").expect {
      supervisionExample.tell(Retrieve(Key(nonExistingKey), storedClient.ref))
    }
    LoggingTestKit.error("I might lost my state").expect {
      supervisionExample.tell(Retrieve(Key(nonExistingKey), storedClient.ref))
    }
    LoggingTestKit.error("I'm stopping").expect {
      supervisionExample.tell(Retrieve(Key(nonExistingKey), storedClient.ref))
    }

    val keyClient = testKit.createTestProbe[Key]()
    supervisionExample.tell(Save("key1", 3, keyClient.ref))
    keyClient.expectNoMessage()
  }
}
