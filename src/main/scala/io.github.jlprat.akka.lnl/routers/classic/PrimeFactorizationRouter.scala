package io.github.jlprat.akka.lnl.routers.classic

import akka.actor.Props
import akka.actor.Actor
import akka.routing.ActorRefRoutee
import akka.routing.Router
import akka.routing.RoundRobinRoutingLogic
import io.github.jlprat.akka.lnl.routers.classic.PrimeFactorizationRouter.PrimeFactor
import akka.actor.Terminated

object PrimeFactorizationRouter {

  trait Command
  case class PrimeFactor(n: Long) extends Command

  def props(): Props = Props.create(classOf[PrimeFactorizationRouter])
}

class PrimeFactorizationRouter extends Actor {

  var router = {
    val routees = Vector.fill(5) {
      val r = context.actorOf(Props[PrimeFactorization]())
      context.watch(r)
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  override def receive: Actor.Receive = {
    case msg: PrimeFactor => router.route(msg, sender())
    case Terminated(a)    =>
      // This will recreate a worker in case of unexpected termination
      router = router.removeRoutee(a)
      val r = context.actorOf(Props[PrimeFactorization]())
      context.watch(r)
      router = router.addRoutee(r)
  }

}
