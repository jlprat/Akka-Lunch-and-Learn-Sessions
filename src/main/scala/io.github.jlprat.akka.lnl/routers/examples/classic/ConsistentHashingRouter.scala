package io.github.jlprat.akka.lnl.routers.examples.classic

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter.ConsistentHashable
import akka.util.Timeout

import ConsistentHashingRouter.KeyValueStore._

/**
  * This uses ConsistentHashingRouter.
  * It will use the routees to spread the key space among them consistently, so each
  * routee will only store a distinct group of keys.
  * We can see looking at the logs that messages with the same key are always processed
  * by the same routee
  */
object ConsistentHashingRouter {

  object KeyValueStore {

    /**
      * All commands should reference a key
      * ConsistentHashable trait requires to implement a consistent hash key
      * In this particular case, the key itself will be the hash key
      */
    trait Command extends ConsistentHashable {
      val key: String
      override def consistentHashKey: Any = key
    }
    case class Put(key: String, value: String) extends Command
    case class Get(key: String)                extends Command
    case class Remove(key: String)             extends Command

    def props(): Props = Props(classOf[KeyValueStore])
  }

  class KeyValueStore extends Actor with ActorLogging {

    var storage: Map[String, String] = Map.empty

    override def receive: Actor.Receive = {
      case Put(key, value) =>
        storage += (key -> value)
        log.info("Saving key {} in actor {}", key, self.path.toStringWithoutAddress)
      case Get(key) =>
        sender() ! storage.get(key)
        log.info("Getting key {} in actor {}", key, self.path.toStringWithoutAddress)
      case Remove(key) =>
        storage -= key
        log.info("Removing key {} in actor {}", key, self.path.toStringWithoutAddress)
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system  = ActorSystem("TailChopping")
    implicit val timeout = Timeout(1.second)

    // This could take a [[ConsistentHashMapping]] function to determine the key of messages.
    // [[ConsistentHashMapping]] can be used when we don't have access to the messages
    val router = system.actorOf(
      ConsistentHashingPool(10).props(Props[KeyValueStore]()),
      name = "keyValueStore"
    )

    // Alternatively, we can wrap a message with [[ConsistentHashableEnvelope]] manually
    // providing the hash key
    router ! Put("City", "Berlin")
    val city = Await.result(router.ask(Get("City")).mapTo[Option[String]], 1.second)
    println(s"City is $city")

    router ! Remove("City")
    val removedCity = Await.result(router.ask(Get("City")).mapTo[Option[String]], 1.second)
    println(s"City is $removedCity")

    router ! Put("Name", "Smith")
    val name = Await.result(router.ask(Get("Name")).mapTo[Option[String]], 1.second)
    println(s"Name is $name")

    Await.result(system.terminate(), 1.second)

    println("Shutting down!")
  }

}
