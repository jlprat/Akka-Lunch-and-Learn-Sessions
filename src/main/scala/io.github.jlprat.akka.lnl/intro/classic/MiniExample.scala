package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.Actor
import akka.actor.Props
import io.github.jlprat.akka.lnl.intro.classic.MiniExample.Click
import io.github.jlprat.akka.lnl.intro.classic.MiniExample.RetrieveClicks

object MiniExample {
  sealed trait Command
  case object Click extends Command
  case object RetrieveClicks extends Command

  def props(): Props = Props(classOf[MiniExample])
}

class MiniExample extends Actor {

  var clicks: Long = 0

  override def receive: Actor.Receive = {
    case Click => clicks = clicks + 1
    case RetrieveClicks => sender() ! clicks
  }
}
