package org.ua.wozzya.data.warehouse.metastore

import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, DataFrameReader, Row, SparkSession}
import org.ua.wozzya.data.warehouse.LazyDataFrame
import org.ua.wozzya.data.warehouse.metastore.Metastore.spark


class Metastore(dfSupplier: () => DataFrame,
                schema: StructType = spark.emptyDataFrame.schema,
                ignoreValidation: Boolean = false)
  extends LazyDataFrame(dfSupplier) {

  def validate(input: StructType, schema: StructType): Boolean = {
    if (input.length != schema.length) return false

    input.map(x => schema.contains(x)).count(!_) == 0
  }

  override def handleLoadedSuccessfully(value: DataFrame): DataFrame = {
    if (ignoreValidation || validate(value.schema, schema)) {
      value
    } else {
      throw new RuntimeException("schemas mismatch")
    }
  }

  override def handleLoadedWithFailure(exception: Throwable): DataFrame = {
    println(exception)
    println("creating empty metastore")
    spark.createDataFrame(spark.sparkContext.emptyRDD[Row], schema)
  }
}


object Metastore {
  lazy val spark: SparkSession = SparkSession.builder.getOrCreate()

  def apply(reader: DataFrameReader,
            schema: StructType,
            ignoreValidation: Boolean = false): Metastore = {
    new Metastore(() => reader.load(), schema, ignoreValidation)
  }
}
