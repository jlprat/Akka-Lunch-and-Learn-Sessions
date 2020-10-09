package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.SupervisorStrategy

object SupervisionExample {

  object Node {

    sealed trait Command
    case class Save(tag: String, value: Int, replyTo: ActorRef[Key]) extends Command
    case class Retrieve(key: Key, replyTo: ActorRef[Stored])         extends Command

    case class Key(id: String)
    case class Stored(product: Product)

    case class Product(hash: String, tag: String, value: Int)

    def apply(storage: Map[String, Product] = Map.empty): Behavior[Command] =
      Behaviors.receiveMessage {
        case Save(tag, value, replyTo) =>
          val hash = s"${tag.hashCode()}"
          replyTo ! Key(hash)
          apply(storage.updated(hash, Product(hash, tag, value)))
        case Retrieve(key, _) if !storage.contains(key.id) =>
          throw new IllegalStateException("No such key!")
        case Retrieve(key, replyTo) =>
          replyTo ! Stored(storage(key.id))
          Behaviors.same

      }
  }

  object Guardian {

    sealed trait Command
    case object Init extends Command

    def supervise(behavior: Behavior[Node.Command]): Behavior[Node.Command] =
      Behaviors.supervise(behavior).onFailure[IllegalStateException](SupervisorStrategy.resume)

    def apply(): Behavior[Command] =
      Behaviors.receive {
        case (context, Init) =>
          context.spawnAnonymous(supervise(Node()))
          initialized()
      }

    def initialized(): Behavior[Command] =
      Behaviors.receiveMessage {
        case _ => throw new IllegalStateException("Already initialized")
      }

  }
}
