package io.github.jlprat.akka.lnl.routers.examples.typed

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.util.Timeout

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.Behavior
import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._

/**
  * This uses ConsistentHashingRouter.
  * It will use the routees to spread the key space among them consistently, so each
  * routee will only store a distinct group of keys.
  * We can see looking at the logs that messages with the same key are always processed
  * by the same routee
  * This variant will have a message (hard reset) broadcasted to all routees.
  */
object ConsistentHashingRouterWithBroadcast {

  sealed trait Command
  case class Put(key: String, value: String)                     extends Command
  case class Get(key: String, replyTo: ActorRef[Option[String]]) extends Command
  case class Remove(key: String)                                 extends Command
  case object HardReset                                          extends Command

  def keyValueStoreBehavior(storage: Map[String, String] = Map.empty): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage[Command] {
        case Get(key, replyTo) =>
          context.log.info(
            "Getting key {} in actor {}",
            key,
            context.self.path.toStringWithoutAddress
          )
          replyTo.tell(storage.get(key))
          Behaviors.same
        case Put(key, value) =>
          context.log.info(
            "Putting key {} in actor {}",
            key,
            context.self.path.toStringWithoutAddress
          )
          keyValueStoreBehavior(storage + (key -> value))
        case Remove(key) =>
          context.log.info(
            "Removing key {} in actor {}",
            key,
            context.self.path.toStringWithoutAddress
          )
          keyValueStoreBehavior(storage - key)
        case HardReset =>
          context.log.info(
            "Wiping all memory in actor {}",
            context.self.path.toStringWithoutAddress
          )
          keyValueStoreBehavior(Map.empty)
      }
    }

  def main(args: Array[String]): Unit = {

    implicit val timeout: Timeout = 3.seconds

    val hashMapping: Command => String = {
      case Put(key, _) => key
      case Get(key, _) => key
      case Remove(key) => key
    }

    val consistentHashingPool = Routers
      .pool(10)(keyValueStoreBehavior(Map.empty))
      .withConsistentHashingRouting(5, hashMapping)
      .withBroadcastPredicate({
        case HardReset => true
        case _         => false
      })
    implicit val router = ActorSystem[Command](consistentHashingPool, "ConsistentHashing")

    router.tell(Put("City", "Berlin"))
    val city = Await.result(router.ask(ref => Get("City", ref)), 1.second)
    println(s"City is $city")

    router ! Put("Name", "Smith")
    val name = Await.result(router.ask(ref => Get("Name", ref)), 1.second)
    println(s"Name is $name")

    router ! HardReset

    val removedCity = Await.result(router.ask(ref => Get("City", ref)), 1.second)
    println(s"City is $removedCity")
    val removedName = Await.result(router.ask(ref => Get("Name", ref)), 1.second)
    println(s"Name is $removedName")

    println("Terminating!")
    router.terminate()
  }

}
