package io.github.jlprat.akka.lnl.supervision.typed

import akka.actor.typed.{ActorRef, Behavior, ChildFailed, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import scala.concurrent.duration._

/**
  * Little simplistic actor-backed key/value store
  */
object SupervisionExample {

  sealed trait Command
  case object Init extends Command

  sealed trait NodeCommand                                         extends Command
  case class Save(tag: String, value: Int, replyTo: ActorRef[Key]) extends NodeCommand
  case class Retrieve(key: Key, replyTo: ActorRef[Stored])         extends NodeCommand

  case class Key(id: String)
  case class Stored(product: Product)

  case class Product(hash: String, tag: String, value: Int)

  /**
    * Supervision on given behavior where actor is allowed to restart 3 times within a second.
    * More frequent restarts than the specified will cause the behavior to stop
    *
    * @param behavior to supervise
    * @return Supervised [[Behavior]]
    */
  def supervise(behavior: Behavior[NodeCommand]): Behavior[NodeCommand] =
    Behaviors
      .supervise(behavior)
      .onFailure[IllegalStateException](SupervisorStrategy.restart.withLimit(3, 1.minute))

  def apply(): Behavior[Command] =
    Behaviors.receive {
      case (context, Init) =>
        val child = context.spawn(supervise(store()), "store")
        context.watch(child)
        initialized(child)
      case _ => throw new IllegalStateException("Not yet initialized")
    }

  def initialized(child: ActorRef[NodeCommand]): Behavior[Command] =
    Behaviors
      .receiveMessage[Command] {
        case Init => throw new IllegalStateException("Already initialized")
        case c @ Save(_, _, _) =>
          child.tell(c)
          Behaviors.same
        case c @ Retrieve(_, _) =>
          child.tell(c)
          Behaviors.same
      }
      .receiveSignal {
        case (context, ChildFailed(_, _)) =>
          // In case the child is stopping due to too many consecutive failures, we stopped this behavior as well.
          context.log.error("I'm stopping")
          Behaviors.stopped
      }

  def store(storage: Map[String, Product] = Map.empty): Behavior[NodeCommand] =
    Behaviors.receive {
      case (_, Save(tag, value, replyTo)) =>
        val hash = s"${tag.hashCode()}"
        replyTo ! Key(hash)
        store(storage.updated(hash, Product(hash, tag, value)))
      case (context, Retrieve(key, _)) if !storage.contains(key.id) =>
        //As the keys are "secret" if somebody is asking for an unknown key, it might mean we lost some state
        context.log.error("I might lost my state")
        throw new IllegalStateException("No such key!")
      case (_, Retrieve(key, replyTo)) =>
        replyTo ! Stored(storage(key.id))
        Behaviors.same
    }
}
