package co.ledger.wallet.web.ethereum.core.database

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}

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
class Cursor[M <: Model] {

  def foreach(f: (Option[M]) => Unit): Unit = foreach(-1)
  def foreach(limit: Int)(f: (Option[M]) => Unit) = foreach(0, limit)(f)
  def foreach(offset: Int, limit: Int)(f: (Option[M]) => Unit) = {
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
}

class WriteCursor[M <: Model] extends Cursor[M] {

  def delete(): Unit = {

  }

  def update(newData: M): Unit = {

  }

}

class CursorBuilder[M <: Model](creator: ModelCreator[M]) {

  def build(): Cursor[M] = {
    null
  }

}
