package io.github.jlprat.akka.lnl.supervision.classic

import akka.actor.{Actor, Props}
import io.github.jlprat.akka.lnl.supervision.classic.Initialization._

object Initialization {
  sealed trait Command
  case object Init extends Command


  object DBConnection

  def props(): Props = Props(classOf[Initialization])
}

class Initialization extends Actor {

  // Constructor initialization
  initDB()

  // Pre Start Hook initialization
  override def preStart(): Unit = {
    initDB()
  }

  override def receive: Actor.Receive = {
    case Init => 
      // Initialization via Message
      initDB()
    case _ => ??? // normal actor work
  }

  private def initDB(): Unit = ()
  
}
