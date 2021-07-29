package org.ua.wozzya.data.warehouse.formation

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.ua.wozzya.data.warehouse.LazyDataFrame
import org.ua.wozzya.data.warehouse.formation.cli.ArgParser
import org.ua.wozzya.data.warehouse.metastore.Metastore
import org.ua.wozzya.data.warehouse.metastore.schema.Schemas
import org.ua.wozzya.util.TimeStamper

import scala.util.{Failure, Success, Try}

object ChannelProcessor {
  def main(args: Array[String]): Unit = {

    val options = ArgParser.parseOptions(args)

    val spark: SparkSession = SparkSession.builder
      .appName("channel-processor")
      .getOrCreate()

//    val metastorePath = "s3n://chaikovskyi-metastore-bucket/channels/"
//    val dwhChannelsPath = "s3n://chaikovskyi-data-warehouse-bucket/channels/"
//    val channelsPath = "s3n://chaikovskyi-data-lake-bucket/input/channels"


    val metastorePath = options('mspath).toString
    val dwhChannelsPath = options('whpath).toString
    val channelsPath = options('rawpath).toString

    val metastoreReader = spark.read
      .format("parquet")
      .option("path", metastorePath)
      .option("inferSchema", "true")
      .option("header", "true")

    val metastore = Metastore(metastoreReader, Schemas.channelsMsSchema).DF

    val rawDataReader = spark.read
      .format("json")
      .option("path", channelsPath)
      .option("header", "true")

    val channels = LazyDataFrame(rawDataReader).DF

    val unprocessedChannels = channels
      .join(metastore.hint("broadcast"),
        channels.col("channelId") === metastore.col("channelId"),
        "left_anti").persist()


    val metastoreNewData = unprocessedChannels

    Try {
      unprocessedChannels.first
    } match {
      case Success(_) =>
        val timeStamp = TimeStamper.getStrTimeNowOfPattern(TimeStamper.yyyyMMdd)
        unprocessedChannels.write
          .format("parquet")
          .mode(SaveMode.Append)
          .option("path", dwhChannelsPath + timeStamp)
          .save()

        metastoreNewData.write
          .format("parquet")
          .mode(SaveMode.Append)
          .option("path", metastorePath)
          .save()
      case Failure(exception) => println(exception)
        println("got empty files, preventing saving empty partitions")
    }
  }
}
