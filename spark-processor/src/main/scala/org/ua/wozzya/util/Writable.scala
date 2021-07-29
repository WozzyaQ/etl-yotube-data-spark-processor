package org.ua.wozzya.util

trait Writable[T] {
  def proc(f: T => Unit)
}
