package org.ua.wozzya.data.warehouse.formation

import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.ua.wozzya.data.warehouse.LazyDataFrame
import org.ua.wozzya.data.warehouse.formation.cli.ArgParser
import org.ua.wozzya.data.warehouse.metastore.Metastore
import org.ua.wozzya.data.warehouse.metastore.schema.Schemas
import org.ua.wozzya.util.TimeStamper

import scala.util.{Failure, Success, Try}

object ChannelVideoProcessor {
  def main(args: Array[String]): Unit = {
    val options = ArgParser.parseOptions(args)

    val spark = SparkSession.builder
      .appName("channel-video-processor")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val metastorePath = options('mspath).toString
    val dwhChannelVideosPath = options('whpath).toString
    val channelVideosPath = options('rawpath).toString


    val metastoreReader = spark.read
      .format("parquet")
      .option("path", metastorePath)
      .option("inferSchema", "true")
      .option("header", "true")

    val metastore = Metastore(metastoreReader, Schemas.channelVideosMsSchema).DF

    val rawDataReader = spark.read
      .format("json")
      .option("path", channelVideosPath)
      .option("header", "true")

    val channelsVideos = LazyDataFrame(rawDataReader).DF


    import spark.implicits._

    val unprocessedChannelVideos = channelsVideos.as("chv")
      .join(metastore.hint("broadcast").as("ms"),
        $"chv.contentDetails.videoId" === $"ms.videoId",
        "left_anti")
      .select("contentDetails.videoId",
        "contentDetails.videoPublishedAt",
        "channelId",
        "channelName")
      .withColumn("videoPublishedAt", col("videoPublishedAt").cast("Date"))
      .persist()

    val metastoreNewData = unprocessedChannelVideos
      .select('channelId, 'videoId)

    Try {
      unprocessedChannelVideos.first
    } match {
      case Success(_) =>
        val timeStamp = TimeStamper.getStrTimeNowOfPattern(TimeStamper.yyyyMMdd)
        unprocessedChannelVideos.write
          .format("parquet")
          .mode(SaveMode.Append)
          .option("path", dwhChannelVideosPath + timeStamp)
          .partitionBy("channelId")
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
