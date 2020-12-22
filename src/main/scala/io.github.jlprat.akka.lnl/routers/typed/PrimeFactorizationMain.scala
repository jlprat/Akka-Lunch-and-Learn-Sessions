package io.github.jlprat.akka.lnl.routers.typed

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.Routers
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.ActorSystem

import scala.concurrent.duration._

object PrimeFactorizationMain {

  def main(args: Array[String]): Unit = {

    val toFactor = Seq(4934578352334L, 196330801L, 217997299L, 231282467L, 4934578352334L,
      196330801L, 217997299L, 231282467L, 4934578352334L, 196330801L, 217997299L, 231282467L,
      4934578352334L, 196330801L, 217997299L, 231282467L)

    val primeFactorizationPool = Behaviors.setup[Unit] { ctx =>
      val pool = Routers.pool(4) {
        Behaviors.supervise(PrimeFactorization()).onFailure(SupervisorStrategy.restart)
      }

      val router = ctx.spawn(pool, "prime-factor-pool")

      toFactor.foreach { n =>
        router.tell(PrimeFactorization.PrimeFactor(n))
      }

      Behaviors.empty
    }

    val system = ActorSystem[Unit](primeFactorizationPool, "PrimeFactorization")

    println("App will shutdown in 10 seconds")
    Thread.sleep(10.seconds.toMillis)

    println("Terminating!")
    system.terminate()

  }
}
