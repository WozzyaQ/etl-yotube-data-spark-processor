package org.ua.wozzya.data.warehouse.formation

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.col
import org.ua.wozzya.data.warehouse.LazyDataFrame
import org.ua.wozzya.data.warehouse.formation.cli.ArgParser
import org.ua.wozzya.data.warehouse.metastore.Metastore
import org.ua.wozzya.data.warehouse.metastore.schema.Schemas
import org.ua.wozzya.util.TimeStamper

import scala.util.{Failure, Success, Try}

object VideoCommentsProcessor {
  def main(args: Array[String]): Unit = {
    val options = ArgParser.parseOptions(args)

    val spark = SparkSession.builder
      .appName("video-comments-processor")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val metastorePath = options('mspath).toString
    val dwhVideoCommentsPath = options('whpath).toString
    val videoCommentsPath = options('rawpath).toString

    val metastoreReader = spark.read
      .format("parquet")
      .option("path", metastorePath)
      .option("inferSchema", "true")
      .option("header", "true")

    val metastore = Metastore(metastoreReader, Schemas.videosCommentsMsSchema).DF

    val rawDataReader = spark.read
      .format("json")
      .option("path", videoCommentsPath)
      .option("header", "true")

    val comments = LazyDataFrame(rawDataReader).DF

    val selectedColumns = Seq(
      "channelId",
      "snippet.totalReplyCount",
      "snippet.videoId",
      "snippet.topLevelComment.snippet.authorChannelId.value",
      "snippet.topLevelComment.snippet.authorDisplayName",
      "snippet.topLevelComment.snippet.likeCount",
      "snippet.topLevelComment.snippet.publishedAt",
      "snippet.topLevelComment.snippet.textOriginal",
      "snippet.topLevelComment.snippet.textDisplay"
    )

    import spark.implicits._

    val unprocessedComments = comments.as("c")
      .join(metastore.hint("broadcast").as("ms"),
        $"c.snippet.videoId" === $"ms.videoId", "left_anti")
      .select(selectedColumns.map(c => col(c)): _*)
      .withColumn("publishedAt", col("publishedAt").cast("date")).persist();


    val metastoreNewData = unprocessedComments.select("videoId").distinct().persist()

    Try {
      unprocessedComments.first
    } match {
      case Success(_) =>
        val timeStamp = TimeStamper.getStrTimeNowOfPattern(TimeStamper.yyyyMMdd)
        unprocessedComments.write
          .format("parquet")
          .mode(SaveMode.Append)
          .option("path", dwhVideoCommentsPath + timeStamp)
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
