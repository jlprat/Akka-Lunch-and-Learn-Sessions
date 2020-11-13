package io.github.jlprat.akka.lnl.persistence.typed

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.ReplyEffect

/**
  * Event Streaming Key Value Store using the power of Akka Persistence
  */
object PersistentKeyValueStore {

  sealed trait PutResponse                       extends CborSerializable
  case class Stored(key: String)                 extends PutResponse
  case class Failed(key: String, reason: String) extends PutResponse

  sealed trait GetResponse            extends CborSerializable
  case class Retrieved(value: String) extends GetResponse
  case class Missing(key: String)     extends GetResponse

  /**
    * Actor receives Commands that need to be validated and processed
    */
  sealed trait Command                                                       extends CborSerializable
  case class Put(key: String, value: String, replyTo: ActorRef[PutResponse]) extends Command
  case class Get(key: String, replyTo: ActorRef[GetResponse])                extends Command

  /**
    * Events are state changes requests for the Key-Value Store
    */
  sealed trait Event                                 extends CborSerializable
  case class KeyValuePut(key: String, value: String) extends Event

  /**
    * Class that holds the inner state of the Key-Value Store
    */
  final case class State(storage: Map[String, String]) extends CborSerializable

  object State {
    val empty = State(Map.empty)
  }

  /**
    * Factory method to create the persistent Behavior
    * @param id Entity ID, is the ID that identifies the storage for this particular Key Value Store
    */
  def apply(id: String): Behavior[Command] =
    EventSourcedBehavior.withEnforcedReplies[Command, Event, State](
      persistenceId = PersistenceId("KeyValueStore", id),
      emptyState = State.empty,
      commandHandler = handleCommand,
      eventHandler = handleEvent
    )

  def handleCommand(state: State, command: Command): ReplyEffect[Event, State] =
    command match {
      case Put(key, value, replyTo) =>
        //Let's crate some arbitrary rules for keys and values
        if (key.length() > 100 || value.length > 500) {
          // Validation didn't hold, we reply and don't persist this Event
          Effect.reply(replyTo)(Failed(key, "Either key or value exceed maximum size"))
        } else {
          // Validation succeeded, we transform the command to an Event, and we persist it.
          Effect.persist(KeyValuePut(key, value)).thenReply(replyTo)(_ => Stored(key))
        }
      case Get(key, replyTo) =>
        Effect.reply(replyTo)(state.storage.get(key).map(Retrieved).getOrElse(Missing(key)))
    }

  /**
    * Event is persisted, we can now update our internal state
    */
  def handleEvent(state: State, event: Event): State =
    event match {
      case KeyValuePut(key, value) => State(state.storage + (key -> value))
    }
}
