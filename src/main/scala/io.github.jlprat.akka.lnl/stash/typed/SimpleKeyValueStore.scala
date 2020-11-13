package io.github.jlprat.akka.lnl.stash.typed

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.blocking
import scala.util.Failure
import scala.util.Success

import akka.Done
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.DispatcherSelector
import akka.actor.typed.scaladsl.Behaviors

/**
  * Key Value Store (with a single node).
  * Messages are only guaranteed to be persisted once the reply is sent
  */
object SimpleKeyValueStore {

  sealed trait PutResponse
  case class Stored(key: String) extends PutResponse
  case class Failed(key: String) extends PutResponse

  sealed trait GetResponse
  case class Retrieved(value: String) extends GetResponse
  case class Missing(key: String)     extends GetResponse

  sealed trait Command
  case class Put(key: String, value: String, replyTo: ActorRef[PutResponse]) extends Command
  case class Get(key: String, replyTo: ActorRef[GetResponse])                extends Command

  private case class KeyValueStored(key: String, value: String, replyTo: ActorRef[PutResponse])
      extends Command
  private case class KeyValueFailed(key: String, value: String, replyTo: ActorRef[PutResponse])
      extends Command

  def apply(storage: Map[String, String] = Map.empty): Behavior[Command] =
    Behaviors.setup { context =>
      implicit val blockingEC: ExecutionContext =
        context.system.dispatchers.lookup(DispatcherSelector.blocking())
      Behaviors.receiveMessage {
        case Put(key, value, replyTo) =>
          // We request a DB save
          val saved = saveToDatabase(key, value)
          // When future is completed, we'll get the right message
          context.pipeToSelf(saved) {
            case Failure(exception) =>
              context.log.error("Error Saving to DB", exception)
              KeyValueFailed(key, value, replyTo)
            case Success(_) =>
              KeyValueStored(key, value, replyTo)
          }
          Behaviors.same
        case Get(key, replyTo) =>
          replyTo.tell(
            storage.get(key).map(Retrieved).getOrElse(Missing(key))
          )
          Behaviors.same
        case KeyValueFailed(key, _, replyTo) =>
          replyTo.tell(Failed(key))
          Behaviors.same
        case KeyValueStored(key, value, replyTo) =>
          // Key-Value can be safely stored now
          replyTo.tell(Stored(key))
          apply(storage + (key -> value))
      }
    }

  /**
    * We pretend this is a costly DB save operation
    */
  @nowarn
  def saveToDatabase(key: String, value: String)(implicit ec: ExecutionContext): Future[Done] =
    Future {
      blocking {
        Thread.sleep(1000L)
        Done
      }
    }
}
