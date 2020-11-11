package io.github.jlprat.akka.lnl.persistence.classic

import akka.actor.Props
import akka.persistence.PersistentActor
import io.github.jlprat.akka.lnl.persistence.classic.PersistentKeyValueStoreWithSnapshot._
import akka.persistence.SnapshotOffer
import akka.actor.ActorLogging
import io.github.jlprat.akka.lnl.persistence.typed.CborSerializable
import akka.persistence.SaveSnapshotSuccess
import akka.persistence.SaveSnapshotFailure

object PersistentKeyValueStoreWithSnapshot {

  case class Failure(msg: String)
  case class Persisted(key: String)

  sealed trait Command
  case class Put(key: String, value: String) extends Command
  case class Get(key: String)                extends Command
  case object BOOM                           extends Command

  sealed trait Event
  case class KeyValuePut(key: String, value: String) extends Event

  final case class State(storage: Map[String, String]) extends CborSerializable

  def props(persistenceID: String): Props =
    Props(classOf[PersistentKeyValueStoreWithSnapshot], persistenceID)
}

class PersistentKeyValueStoreWithSnapshot(val persistenceID: String)
    extends PersistentActor
    with ActorLogging {

  var state            = State(Map.empty)
  val snapShotInterval = 100

  override def persistenceId: String = persistenceID

  private def updateState(key: String, value: String): Unit = {
    state = State(state.storage + (key -> value))
  }

  override def receiveRecover: PartialFunction[Any, Unit] = {
    case KeyValuePut(key, value) =>
      log.info("event restored")
      updateState(key, value)
    case SnapshotOffer(_, snapshot: State) =>
      log.info("Snapshot restored")
      state = snapshot
  }

  override def receiveCommand: PartialFunction[Any, Unit] = {
    case Put(key, value) =>
      if (key.length() > 100 || value.length > 500) {
        sender() ! Failure("Either key or value exceed maximum size")
      } else {
        persist(KeyValuePut(key, value)) { event =>
          updateState(event.key, event.value)
          sender() ! Persisted(key)
          if (lastSequenceNr % snapShotInterval == 0 && lastSequenceNr != 0) {
            log.info("snapshot to be saved!")
            saveSnapshot(state)
          }
        }
      }
    case Get(key) =>
      sender() ! state.storage.get(key)
    case BOOM                           => throw new RuntimeException("Kaboom!")
    case SaveSnapshotSuccess(_)         => log.info("snapshot saved")
    case SaveSnapshotFailure(_, reason) => log.info(s"snapshot failed $reason")
  }

}
