package org.ua.wozzya.analytics

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.IntegerType
import org.ua.wozzya.util.TimeStamper

import scala.sys.exit
import scala.util.{Failure, Success, Try}

object Top100 {
  def main(args: Array[String]): Unit = {

    val date = Try {
      args(0)
    } match {
      case Success(date) => date
      case Failure(ex) =>
        println("The date should be provided as an argument")
        println("Format yyyy-MM-dd")
        exit(-1)
    }

    val topN = args(1).toInt

    val spark: SparkSession = SparkSession.builder
      .appName("top-100-commented")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")


    // TODO unbind pathes and pass them through app arguments
    val channels = spark.read.parquet("s3n://chaikovskyi-data-warehouse-bucket/channels/*/*")
    val channelVideo = spark.read.parquet("s3n://chaikovskyi-data-warehouse-bucket/channel-videos/*/*/*")
    val videoComment = spark.read.parquet("s3n://chaikovskyi-data-warehouse-bucket/video-comments/*/*/*")
    val blacklist = spark.read.text("s3n://chaikovskyi-data-lake-bucket/input/blacklist")


    val channelVideoWithIds = channelVideo.as("channelVideo").join(channels.as("channels"),
      col("channelVideo.channelName") === col("channels.channelName"),
      "left")
      .select(
        "channelVideo.channelName",
        "channels.channelId",
        "channelVideo.videoId",
        "channelVideo.videoPublishedAt")
      .filter(col("channelVideo.videoPublishedAt").gt(date
      ))


    val filteredChannelVideo = channelVideoWithIds.as("cs")
      .join(blacklist.hint("broadcast"),
        col("channelId") === col("value"),
        "left_anti")


    val joined = videoComment.as("vc")
      .join(filteredChannelVideo.hint("broadcast").as("cv"),
        col("vc.videoId") === col("cv.videoId"),
        "right")
      .select("cv.channelName", "vc.videoId", "cv.videoPublishedAt", "vc.likeCount", "vc.totalReplyCount")
      .withColumnRenamed("likeCount", "commentLikeCount")
      .withColumnRenamed("totalReplyCount", "replies")


    val top100Commented = joined.withColumn("total", col("replies").cast(IntegerType))
      .groupBy("videoId").agg(
      first("cv.channelName").alias("channel"),
      (sum("replies").alias("repl") + count("replies")).alias("rating")
    )
      .orderBy(desc("rating"))
      .filter(col("rating").isNotNull)
      .withColumn("videoId", concat(lit("https://www.youtube.com/watch?v="),col("videoId")))
      .limit(100).persist()


    val timeStamp = TimeStamper.getStrTimeNowOfPattern(TimeStamper.yyyyMMdd)

    top100Commented.coalesce(1)
      .write.format("csv")
      .option("header", "true")
      .option("encoding", "UTF-8")
      .mode(SaveMode.Overwrite)
      .save(s"s3n://chaikovskyi-report-bucket/$timeStamp-top-$topN-after-$date")
  }
}
