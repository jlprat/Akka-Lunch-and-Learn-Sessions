package io.github.jlprat.akka.lnl.routers.examples.classic

import java.io.FileWriter

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.BroadcastPool

import BroadcastRouter.LogStore._

/**
  * This uses BroadcastRouter.
  * We want availability at the cost of high duplication.
  * Each message will be sent to all Routees
  */
object BroadcastRouter {

  object LogStore {

    trait Command
    case class Put(line: String) extends Command

    def props(): Props = Props(classOf[LogStore])
  }

  class LogStore extends Actor with ActorLogging {

    val file = new FileWriter(s"/tmp/logger${self.path.name}.log", true)

    def saveToDisk(line: String): Unit = {
      val _ = file.append(line + "\n")
    }

    override def receive: Actor.Receive = {
      case Put(line) =>
        saveToDisk(line)
        log.info("Saving log line {} in actor {}", line, self.path.toStringWithoutAddress)

    }

    override def postStop(): Unit = {
      file.close()
    }
  }

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("BroadcastRouter")

    val router =
      system.actorOf(BroadcastPool(3).props(LogStore.props()), name = "RedundantLogStore")

    router ! Put("This is a log line")
    router ! Put("Yet another")

    Await.result(system.terminate(), 1.second)

    println("Shutting down!")
  }
}
