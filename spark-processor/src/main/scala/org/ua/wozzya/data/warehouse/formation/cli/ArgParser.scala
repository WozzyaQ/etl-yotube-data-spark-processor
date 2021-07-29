package org.ua.wozzya.data.warehouse.formation.cli

import scala.annotation.tailrec
import scala.sys.exit

object ArgParser {

  type OptionMap = Map[Symbol, Any]
  val usage: String =
    """
      |input parameters: --metastore-path <path> --raw-data-path <path> --warehouse-path <path>
      |""".stripMargin


  def printUsage(): Unit = println(usage)

  def parseOptions(args: Array[String]): OptionMap = {
    if (args.length == 0) {
      printUsage()
      exit(-1)
    }

    @tailrec
    def nextOption(map: OptionMap, list: List[String]): OptionMap = {
      list match {
        case Nil => map
        case "--metastore-path" :: value :: tail => nextOption(map ++ Map('mspath -> value), tail)
        case "--warehouse-path" :: value :: tail => nextOption(map ++ Map('whpath -> value), tail)
        case "--raw-data-path" :: value :: tail => nextOption(map ++ Map('rawpath -> value), tail)
        case option :: _ => println("unknown option" + option)
          exit(-1)
      }
    }

    nextOption(Map(), args.toList)
  }
}
