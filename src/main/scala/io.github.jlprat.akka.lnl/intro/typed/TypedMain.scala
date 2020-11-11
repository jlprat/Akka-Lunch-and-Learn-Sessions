package io.github.jlprat.akka.lnl.intro.typed

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.io.StdIn

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import io.github.jlprat.akka.lnl.intro.typed.TemperatureStatistics.GetAverageTemperature

object TypedMain {

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem[TemperatureStatistics.Command] =
      ActorSystem(TemperatureStatistics(), "CPUTemperature")

    implicit val timeout: Timeout = Timeout(100.millis)
    implicit val ec               = system.executionContext

    val promise = Promise[Done]()
    
    Future {
      blocking {
        while (!promise.isCompleted) {
          Thread.sleep(3000)
          system.ask(ref => GetAverageTemperature(ref)).foreach(temp => println(s"Average $temp"))
        }
      }
    }

    if (StdIn.readLine("Press RETURN to stop...\n") != null) {
      promise.trySuccess(Done)
    }

    val _ = system.terminate()
  }

}
