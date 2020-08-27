package io.github.jlprat.akka.lnl.intro.util

import java.io.{BufferedReader, InputStreamReader}

import scala.concurrent.{blocking, ExecutionContext, Future}

object Util {
  def getTemperature(ec: ExecutionContext): Future[Double] =
  
    Future {
      blocking {
        val reader = new BufferedReader(
          new InputStreamReader(
            Runtime.getRuntime().exec("acpi -t | cut -d ',' -f2").getInputStream()
          )
        )
        reader.readLine().toDouble
      }
    }(ec)
}
