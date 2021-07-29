package org.ua.wozzya.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object TimeStamper {
  val yyyyMMdd = "yyyy-MM-dd"

  def getStrTimeNowOfPattern(pattern: String): String = {
    LocalDate.now().format(DateTimeFormatter.ofPattern(pattern))
  }
}
