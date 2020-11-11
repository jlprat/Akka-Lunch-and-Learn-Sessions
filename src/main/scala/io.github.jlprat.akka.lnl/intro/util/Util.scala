package io.github.jlprat.akka.lnl.intro.util

import java.io.BufferedReader
import java.io.InputStreamReader

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.blocking

object Util {
  def getTemperature(ec: ExecutionContext): Future[Double] = {

    val cmds = Array("/bin/sh", "-c", "acpi -t | cut -d ',' -f2 | cut -d ' ' -f2")

    Future {
      blocking {
        val reader = new BufferedReader(
          new InputStreamReader(
            Runtime.getRuntime().exec(cmds).getInputStream()
          )
        )
        reader.readLine().toDouble
      }
    }(ec)
  }
}
