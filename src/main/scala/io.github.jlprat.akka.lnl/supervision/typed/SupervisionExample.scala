package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._

object SupervisionExample {

  sealed trait Command
  case object Init extends Command

  sealed trait NodeCommand                                         extends Command
  case class Save(tag: String, value: Int, replyTo: ActorRef[Key]) extends NodeCommand
  case class Retrieve(key: Key, replyTo: ActorRef[Stored])         extends NodeCommand

  case class Key(id: String)
  case class Stored(product: Product)

  case class Product(hash: String, tag: String, value: Int)

  def supervise(behavior: Behavior[NodeCommand]): Behavior[NodeCommand] =
    Behaviors
      .supervise(behavior)
      .onFailure[IllegalStateException](SupervisorStrategy.restart.withLimit(3, 1.minute))

  def apply(): Behavior[Command] =
    Behaviors.receive {
      case (context, Init) =>
        val child = context.spawnAnonymous(supervise(store()))
        initialized(child)
      case _ => throw new IllegalStateException("Not yet initialized")
    }

  def initialized(child: ActorRef[NodeCommand]): Behavior[Command] =
    Behaviors.receiveMessage {
      case Init => throw new IllegalStateException("Already initialized")
      case c @ Save(_, _, _) =>
        child.tell(c)
        Behaviors.same
      case c @ Retrieve(_, _) =>
        child.tell(c)
        Behaviors.same
    }

  def store(storage: Map[String, Product] = Map.empty): Behavior[NodeCommand] =
    Behaviors.receiveMessage {
      case Save(tag, value, replyTo) =>
        val hash = s"${tag.hashCode()}"
        replyTo ! Key(hash)
        store(storage.updated(hash, Product(hash, tag, value)))
      case Retrieve(key, _) if !storage.contains(key.id) =>
        throw new IllegalStateException("No such key!")
      case Retrieve(key, replyTo) =>
        replyTo ! Stored(storage(key.id))
        Behaviors.same
    }
}
