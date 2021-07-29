package org.ua.wozzya.data.warehouse

import org.apache.spark.sql.{DataFrame, DataFrameReader}
import org.ua.wozzya.util.Writable

import scala.util.{Failure, Success, Try}


abstract class Lazy[T](f: () => T) {
  private lazy val res = Try(f())

  protected def value: T = res.get
}

abstract class AbstractLazyDataFrame(f: () => DataFrame)
  extends Lazy[DataFrame](f) with Writable[DataFrame] {
  lazy val DF: DataFrame = Try {
    value
  } match {
    case Success(value) => handleLoadedSuccessfully(value)
    case Failure(exception) => handleLoadedWithFailure(exception)
  }

  def handleLoadedSuccessfully(value: DataFrame): DataFrame

  def handleLoadedWithFailure(exception: Throwable): DataFrame

  override def proc(f: DataFrame => Unit): Unit = {
    f(value)
  }
}

class LazyDataFrame(f: () => DataFrame)
  extends AbstractLazyDataFrame(f) {
  override def handleLoadedSuccessfully(value: DataFrame): DataFrame = {
    value
  }

  override def handleLoadedWithFailure(exception: Throwable): DataFrame = {
    println(exception)
    throw new RuntimeException("fail to load with exception " + exception.getMessage)
  }
}

object LazyDataFrame {
  def apply(df: DataFrame): LazyDataFrame = {
    new LazyDataFrame(() => df)
  }

  def apply(reader: DataFrameReader): LazyDataFrame = {
    new LazyDataFrame(() => reader.load())
  }
}