package org.ua.wozzya.data.warehouse.metastore.schema

import org.apache.spark.sql.types.{StringType, StructField, StructType}

object Schemas {

  lazy val channelsMsSchema: StructType = StructType(
    StructField("channelName", StringType) ::
      StructField("channelId", StringType) :: Nil
  )

  lazy val channelVideosMsSchema: StructType = StructType(
    StructField("channelId", StringType) ::
      StructField("videoId", StringType) :: Nil
  )

  lazy val videosCommentsMsSchema: StructType = StructType(
    StructField("videoId", StringType)
      :: Nil
  )
}
