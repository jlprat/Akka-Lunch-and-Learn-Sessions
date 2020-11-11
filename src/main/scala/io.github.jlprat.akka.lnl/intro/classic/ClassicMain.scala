package io.github.jlprat.akka.lnl.intro.classic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Promise
import scala.concurrent.blocking
import scala.concurrent.duration._
import scala.io.StdIn

import akka.Done
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import io.github.jlprat.akka.lnl.intro.classic.TemperatureStatistics.GetAverageTemperature

object ClassicMain {

  def main(args: Array[String]): Unit = {

    val system    = ActorSystem("CPUTemperature")
    val tempStats = system.actorOf(TemperatureStatistics.props(), "stats")

    implicit val timeout: Timeout = Timeout(100.millis)

    val promise = Promise[Done]()

    Future {
      blocking {
        while (!promise.isCompleted) {
          Thread.sleep(3000)
          (tempStats ? GetAverageTemperature).foreach(temp => println(s"Average $temp"))
        }
      }
    }

    if (StdIn.readLine("Press RETURN to stop...\n") != null) {
      promise.trySuccess(Done)
    }
    val _ = system.terminate()
  }
}
