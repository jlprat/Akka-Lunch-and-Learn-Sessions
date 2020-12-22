package io.github.jlprat.akka.lnl.routers.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.ActorSystem
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.receptionist.Receptionist

import scala.concurrent.duration._

/**
  * This uses Random strategy.
  * There is no way we can predict how work will be split among routees.
  */
object PrimeFactorizationGroupMain {

  def main(args: Array[String]): Unit = {

    val serviceKey = ServiceKey[PrimeFactorization.Command]("prime-factorization-worker")

    val toFactor = Seq(4934578352334L, 196330801L, 217997299L, 231282467L, 4934578352334L,
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L,
      4934578352334L, 196330801L, 217997299L, 231282467L)

    val initiator = Behaviors.setup[Unit] { ctx =>
      val routees = (1 to 4).map(i => ctx.spawn(PrimeFactorization(), s"Routee$i")).toList
      routees.foreach { actorRef =>
        ctx.system.receptionist.tell(Receptionist.Register(serviceKey, actorRef))
      }

      val group = Routers.group(serviceKey)

      val router = ctx.spawn(group, "prime-factor-pool")

      toFactor.foreach { n =>
        router.tell(PrimeFactorization.PrimeFactor(n))
      }

      Behaviors.empty
    }

    val system = ActorSystem[Unit](initiator, "PrimeFactorizationGroup")

    println("App will shutdown in 10 seconds")
    Thread.sleep(10.seconds.toMillis)

    println("Terminating!")
    system.terminate()

  }
}
