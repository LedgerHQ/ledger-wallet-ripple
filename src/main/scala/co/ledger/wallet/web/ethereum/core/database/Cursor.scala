package co.ledger.wallet.web.ethereum.core.database

import org.scalajs.dom.idb

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * Cursor
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 08/06/2016.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
class Cursor[M <: Model](request: idb.Request)(implicit classTag: ClassTag[M]) {

  def foreach(f: (Option[M]) => Unit): Unit = foreach(-1)(f)
  def foreach(limit: Int)(f: (Option[M]) => Unit): Unit = foreach(0, limit)(f)
  def foreach(offset: Int, limit: Int)(f: (Option[M]) => Unit): Unit = {
    var index = 0
    val maxIndex = offset + limit
    foreach {(item) =>
      if (limit > 0 && index >= maxIndex) {
        close()
      } else if (index >= offset) {
        f(item)
      }
      index += 1
    }
  }

  def toArray: Future[Array[M]] = toArray(-1)
  def toArray(limit: Int): Future[Array[M]] = toArray(0, limit)
  def toArray(offset: Int, limit: Int): Future[Array[M]] = {
    val promise = Promise[Array[M]]()
    val result = ArrayBuffer[M]()
    foreach(offset, limit) {
      case Some(item) => result.append(item)
      case None => promise.success(result.toArray)
    }
    promise.future
  }

  def advance(n: Int): Future[Unit] = ???
  def continue(): Future[Unit] = ???
  def offset: Int = ???
  def value: Option[M] = ???

  def close(): Unit = {

  }

  def futureValue: Future[M] = ???
}

class WriteCursor[M <: Model](request: idb.Request)(implicit classTag: ClassTag[M]) extends Cursor[M](request) {

  def delete(): Unit = {

  }

  def update(newData: M): Unit = {

  }

}

trait CursorBuilder[M <: Model] {
  protected implicit val modelClassTag: ClassTag[M]
  protected val modelDeclaration: M
  protected val creator: ModelCreator[M]
  protected def useWriteCursor() = _writable = true
  protected def buildCursor(transaction: idb.Transaction): Future[Cursor[M]] = {
    val store = transaction.objectStore(modelDeclaration.entityName)
    val direction = "continue"
    val request = indexName match {
      case Some(name) =>
        store.index(name).openCursor()
      case None =>
        store.openCursor()
    }
    val cursor = {
      if (_writable)
        new WriteCursor[M](request)
      else
        new Cursor[M](request)
    }
    cursor.futureValue.map((_) => cursor)
  }
  def reverse(): this.type = {
    this
  }
  def unique(): this.type  = {
    this
  }
  protected var indexName: Option[String] = None
  private var _writable = false
}
