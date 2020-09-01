package io.github.jlprat.akka.lnl.intro.classic

import akka.actor.ActorSystem
import akka.Done
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{blocking, Future, Promise}
import scala.io.StdIn

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
