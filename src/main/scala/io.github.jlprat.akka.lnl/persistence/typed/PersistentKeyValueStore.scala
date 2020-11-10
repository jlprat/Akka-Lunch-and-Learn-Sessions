package io.github.jlprat.akka.lnl.persistence.typed

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect

object PersistentKeyValueStore {

  sealed trait PutResponse                       extends CborSerializable
  case class Stored(key: String)                 extends PutResponse
  case class Failed(key: String, reason: String) extends PutResponse

  sealed trait GetResponse            extends CborSerializable
  case class Retrieved(value: String) extends GetResponse
  case class Missing(key: String)     extends GetResponse

  sealed trait Command                                                       extends CborSerializable
  case class Put(key: String, value: String, replyTo: ActorRef[PutResponse]) extends Command
  case class Get(key: String, replyTo: ActorRef[GetResponse])                extends Command

  sealed trait Event                                 extends CborSerializable
  case class KeyValuePut(key: String, value: String) extends Event

  final case class State(storage: Map[String, String]) extends CborSerializable

  object State {
    val empty = State(Map.empty)
  }

  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId("KeyValueStore", id),
      emptyState = State.empty,
      commandHandler = handleCommand,
      eventHandler = handleEvent
    )

  def handleCommand(state: State, command: Command): Effect[Event, State] =
    command match {
      case Put(key, value, replyTo) =>
        //Let's crate some arbitrary rules for keys and values
        if (key.length() > 100 || value.length > 500) {
          // Validation didn't hold, we reply and don't persist this Event
          replyTo.tell(Failed(key, "Either key or value exceed maximum size"))
          Effect.none
        } else {
          // Validation succeeded, we transform the command to an Event, and we persist it.
          Effect.persist(KeyValuePut(key, value)).thenReply(replyTo)(_ => Stored(key))
        }
      case Get(key, replyTo) =>
        replyTo.tell(state.storage.get(key).map(Retrieved).getOrElse(Missing(key)))
        Effect.none
    }

  def handleEvent(state: State, event: Event): State =
    event match {
      case KeyValuePut(key, value) => State(state.storage + (key -> value))
    }
}
