package co.ledger.wallet.web.ethereum.core.database

import co.ledger.wallet.web.ethereum.core.sjcl.SjclAesCipher
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
class Cursor[M >: Null <: Model](request: idb.Request, creator: ModelCreator[M], password: Option[String])(implicit classTag: ClassTag[M]) {

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
      _valuePromise.success(creator(_cursor.asInstanceOf[js.Dynamic].value.asInstanceOf[js.Dictionary[js.Any]], password))
    else
      _valuePromise.success(null)
  }
  request.onerror = {(event: ErrorEvent) =>
    _valuePromise.failure(new Exception(event.message))
  }
}

class WriteCursor[M >: Null <: Model](request: idb.Request, creator: ModelCreator[M], password: Option[String])(implicit classTag: ClassTag[M]) extends Cursor[M](request, creator, password) {

  def delete(): Unit = {
    _cursor.delete()
  }

  def update(newData: M): Future[Unit] = {
    val request = _cursor.update(newData.toDictionary(password))
    val promise = Promise[Unit]()
    request.onsuccess = {(_: js.Any) =>
      promise.success()
    }
    request.onerror = {(error: ErrorEvent) =>
      promise.failure(new Exception(error.message))
    }
    promise.future
  }

  def deleteAll(predicate: (M) => Boolean): Future[Int] = {
    var count = 0
    def iterate(): Future[Unit] = {
      if (value.isEmpty) {
        Future.successful()
      } else {
        if (predicate(value.get)) {
          count += 1
          delete()
        }
        continue() flatMap {(_) => iterate()}
      }
    }
    iterate() map {(_) =>
      count
    }
  }

}

trait CursorBuilder[M >: Null <: Model] {
  protected implicit val modelClassTag: ClassTag[M]
  protected val modelDeclaration: M
  protected val creator: ModelCreator[M]
  protected val password: Option[String]
  private lazy val modelStructure = creator.newInstance().structure
  private lazy val cipher = new SjclAesCipher(password.get)
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
        new WriteCursor[M](request, creator, password)
      else
        new Cursor[M](request, creator, password)
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

  private def encryptSingleRangeItem(data: js.Any, fieldName: String, throwIfEncrypted: Boolean): js.Any = {
    val field = modelStructure(fieldName)
    if (field.isEncrypted && throwIfEncrypted) {
      throw new Exception("Encrypted field can only be used in equality ranges")
    } else if (field.isEncrypted) {
      s"encrypted:${cipher.encrypt(data.toString)}"
    } else {
      data
    }
  }

  private def encryptRange(data: ArrayBuffer[js.Any], throwIfEncrypted: Boolean): js.Array[js.Any] = {
    var index = -1
    val indexes = indexName.get.split(",")
    js.Array(data.map({(item) =>
      index += 1
      encryptSingleRangeItem(item, indexes(index), throwIfEncrypted)
    }):_*)
  }

  private def encryptRange(data: js.Any, throwIfEncrypted: Boolean): js.Any = {
    encryptSingleRangeItem(data, modelStructure.find(_._2.isUnique).get._1, throwIfEncrypted)
  }

  private def createRange(): idb.KeyRange = {
    if (_eq.isEmpty && _gt.isEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      null
    } else if (_eq.nonEmpty && _gt.isEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      // ==
      if (indexName.nonEmpty)
        idb.KeyRange.only(encryptRange(_eq, false))
      else
        idb.KeyRange.only(encryptRange(_eq.head, false))
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.isEmpty && _lte.isEmpty && _gte.isEmpty) {
      // <
      if (indexName.nonEmpty)
        idb.KeyRange.lowerBound(encryptRange(_lt, true), true)
      else
        idb.KeyRange.lowerBound(encryptRange(_lt.head, true), true)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.isEmpty) {
      // >
      idb.KeyRange.upperBound(encryptRange(_gt, true), true)
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.isEmpty) {
      // > <
      idb.KeyRange.bound(encryptRange(_lt, true), encryptRange(_gt, true), true, true)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.isEmpty && _lte.nonEmpty && _gte.nonEmpty) {
      // >= <=
      idb.KeyRange.bound(encryptRange(_lte, true), encryptRange(_gte, true), false, false)
    } else if (_eq.isEmpty && _gt.nonEmpty && _lt.isEmpty && _lte.nonEmpty && _gte.isEmpty) {
      // > <=
      idb.KeyRange.bound(encryptRange(_lte, true), encryptRange(_gt, true), true, false)
    } else if (_eq.isEmpty && _gt.isEmpty && _lt.nonEmpty && _lte.isEmpty && _gte.nonEmpty) {
      // >= <
      idb.KeyRange.bound(encryptRange(_lte, true), encryptRange(_gt, true), false, true)
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
