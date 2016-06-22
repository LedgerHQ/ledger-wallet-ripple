package co.ledger.wallet.web.ethereum.core.database

import org.scalajs.dom.{ErrorEvent, Event, idb}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

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
class Cursor[M >: Null <: Model](request: idb.Request, creator: ModelCreator[M])(implicit classTag: ClassTag[M]) {

  def foreach(f: (Option[M]) => Unit): Unit = foreach(-1)(f)
  def foreach(limit: Int)(f: (Option[M]) => Unit): Unit = foreach(0, limit)(f)
  def foreach(offset: Int, limit: Int)(f: (Option[M]) => Unit): Unit = {
    var index = 0
    val maxIndex = offset + limit
    def iterate(): Unit = {
      if (!isClosed) {
        continue() foreach {(_) =>
          handleValue()
        }
      }
    }
    def handleValue(): Unit = {
      if (limit > 0 && index >= maxIndex) {
        close()
      } else if (index >= offset) {
        f(value)
      }
      index += 1
      iterate()
    }
    handleValue()
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

  def advance(n: Int): Future[Unit] = {
    if (n == 0)
      Future.successful()
    else {
      futureValue.flatMap { (_) =>
        _cursor.advance(n)
        _valuePromise = Promise()
        _valuePromise.future.map((_) => ())
      }
    }
  }
  def continue(): Future[Unit] = {
    futureValue.flatMap {(_) =>
      _cursor.continue()
      _valuePromise = Promise()
      _valuePromise.future.map((_) => ())
    }
  }
  def value: Option[M] = futureValue.value.flatMap(_.toOption).flatMap(Option(_))

  def close(): Unit = {
    _closed = true
  }

  def isClosed = _closed
  def futureValue: Future[M] = _valuePromise.future
  private var _closed = false
  protected var _cursor: idb.Cursor = null
  private var _valuePromise = Promise[M]()
  request.onsuccess = {(event: Event) =>
    _cursor = event.target.asInstanceOf[js.Dynamic].result.asInstanceOf[idb.Cursor]
    if (_cursor != null)
      _valuePromise.success(creator(_cursor.asInstanceOf[js.Dynamic].value.asInstanceOf[js.Dictionary[js.Any]]))
    else
      _valuePromise.success(null)
  }
  request.onerror = {(event: ErrorEvent) =>
    _valuePromise.failure(new Exception(event.message))
  }
}

class WriteCursor[M >: Null <: Model](request: idb.Request, creator: ModelCreator[M])(implicit classTag: ClassTag[M]) extends Cursor[M](request, creator) {

  def delete(): Unit = {
    _cursor.delete()
  }

  def update(newData: M): Future[Unit] = {
    val request = _cursor.update(newData.toDictionary)
    val promise = Promise[Unit]()
    request.onsuccess = {(_: js.Any) =>
      promise.success()
    }
    request.onerror = {(error: ErrorEvent) =>
      promise.failure(new Exception(error.message))
    }
    promise.future
  }

}

trait CursorBuilder[M >: Null <: Model] {
  protected implicit val modelClassTag: ClassTag[M]
  protected val modelDeclaration: M
  protected val creator: ModelCreator[M]
  protected def useWriteCursor() = _writable = true
  protected def buildCursor(transaction: idb.Transaction): Future[Cursor[M]] = {
    val store = transaction.objectStore(modelDeclaration.entityName)
    val direction = {
      if (!_reverse && !_unique)
        "next"
      else if (_reverse && !_unique)
        "prev"
      else if (!_reverse && _unique)
        "nextunique"
      else
        "prevunique"
    }
    val range: idb.KeyRange = createRange()
    val request = indexName match {
      case Some(name) =>
        store.index(name).openCursor(range, direction)
      case None =>
        store.openCursor(range, direction)
    }
    val cursor = {
      if (_writable)
        new WriteCursor[M](request, creator)
      else
        new Cursor[M](request, creator)
    }
    cursor.futureValue.map((_) => cursor)
  }

  def reverse(): this.type = {
    _reverse = true
    this
  }

  def uniq(): this.type  = {
    _unique = true
    this
  }

  def gt(values: js.Any*): this.type = {
    _gt.appendAll(values)
    this
  }

  def gte(values: js.Any*): this.type = {
    _gte.appendAll(values)
    this
  }

  def lt(values: js.Any*): this.type = {
    _lt.appendAll(values)
    this
  }

  def lte(values: js.Any*): this.type = {
    _lte.appendAll(values)
    this
  }

  def exactly(values: js.Any*): this.type = {
    _eq.appendAll(values)
    this
  }

  protected def count(transaction: idb.Transaction): Future[Long] = {
    val range = Option(createRange())
    val store = transaction.objectStore(modelDeclaration.entityName)
    val request = indexName match {
      case Some(name) =>
        store.index(name).count(range.orNull)
      case None =>
        store.count(range.orNull)
    }
    val promise = Promise[Long]()
    request.onsuccess = {(event: Event) =>
      js.Dynamic.global.console.log(indexName.toString, request, event)
      promise.success(request.result.asInstanceOf[Double].toLong)
    }
    request.onerror = {(event: ErrorEvent) =>
      promise.failure(new Exception(event.message))
    }
    promise.future
  }

  private def createRange(): idb.KeyRange = {
    if (_eq.isEmpty && _gt.isEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      null
    } else if (_eq.nonEmpty && _gt.isEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      // ==
      if (indexName.nonEmpty)
        idb.KeyRange.only(js.Array(_eq:_*))
      else
        idb.KeyRange.only(_eq.head)
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      // <
      if (indexName.nonEmpty)
        idb.KeyRange.lowerBound(js.Array(_lt:_*), true)
      else
        idb.KeyRange.lowerBound(_lt.head, true)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.isEmpty) {
      // >
      idb.KeyRange.upperBound(js.Array(_gt:_*), true)
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.isEmpty) {
      // > <
      idb.KeyRange.bound(js.Array(_lt:_*), js.Array(_gt:_*), true, true)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.isEmpty && _lte.nonEmpty && _gte.nonEmpty) {
      // >= <=
      idb.KeyRange.bound(js.Array(_lte:_*), js.Array(_gte:_*), false, false)
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.isEmpty && _lte.nonEmpty && _gte.isEmpty) {
      // > <=
      idb.KeyRange.bound(js.Array(_lte:_*), js.Array(_gt:_*), true, false)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.nonEmpty) {
      // >= <
      idb.KeyRange.bound(js.Array(_lte:_*), js.Array(_gt:_*), false, true)
    } else {
      throw new Exception("Invalid range")
    }
  }

  protected var indexName: Option[String] = None
  private var _writable = false
  private var _reverse = false
  private var _unique = false
  private val _gt = ArrayBuffer[js.Any]()
  private val _gte = ArrayBuffer[js.Any]()
  private val _eq = ArrayBuffer[js.Any]()
  private val _lt = ArrayBuffer[js.Any]()
  private val _lte = ArrayBuffer[js.Any]()
}
