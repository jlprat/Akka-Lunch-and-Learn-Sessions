package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.Actor
import akka.actor.Props
import io.github.jlprat.akka.lnl.intro.classic.MiniExampleBecome.Click
import io.github.jlprat.akka.lnl.intro.classic.MiniExampleBecome.RetrieveClicks

object MiniExampleBecome {
  sealed trait Command
  case object Click extends Command
  case object RetrieveClicks extends Command

  def props(): Props = Props(classOf[MiniExampleBecome])
}

class MiniExampleBecome extends Actor {

  override def receive: Actor.Receive = clickCounting(0)

  private def clickCounting(count: Long): Actor.Receive = {
    case Click => context.become(clickCounting(count + 1))
    case RetrieveClicks => sender() ! count
  }
  
}
