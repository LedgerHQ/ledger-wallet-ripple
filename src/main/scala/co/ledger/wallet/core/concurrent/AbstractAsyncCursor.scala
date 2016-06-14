/**
  *
  * AbstractAsyncCursor
  * Ledger wallet
  *
  * Created by Pierre Pollastri on 11/01/16.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 Ledger
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
package co.ledger.wallet.core.concurrent

import scala.collection.mutable
import scala.concurrent.{ExecutionContext => EC, Future, Promise}
import scala.reflect.ClassTag
import scala.util.Try

abstract class AbstractAsyncCursor[A : ClassTag](executionContext: EC, override val chunkSize: Int)
  extends AsyncCursor[A] {

  implicit val ec = executionContext

  require(chunkSize > 0, "Chunk size must be more than 0")

  protected def performQuery(from: Int, to: Int): Array[A]

  override def loadAllChunks(): Future[Array[A]] = {
    Future.sequence((0 until chunkCount) map loadChunk).map(_.flatten.toArray)
  }

  override def loadChunk(index: Int): Future[Array[A]] = {
    _chunks.synchronized {
      if (_chunks.contains(index) && (!_chunks(index).isCompleted || _chunks(index).value.get.isSuccess)) {
        _chunks(index)
      } else {
        val p = Promise[Array[A]]()
        ec.execute(new Runnable {
          override def run(): Unit =
            p.complete(Try(performQuery(index * chunkSize,
              Math.min(index * chunkSize + chunkSize, count))))
        })
        _chunks(index) = p.future
        p.future
      }
    }
  }

  override def chunk(index: Int): Option[Array[A]] = {
    ensureNotClosed()
    _chunks synchronized {
      _chunks.lift(index).flatMap(_.value).flatMap(_.toOption)
    }
  }

  override def close(): Unit = {
    ensureNotClosed()
    _chunks synchronized {
      _chunks.clear()
      _isClosed = true
    }
  }

  override def item(index: Int): Option[A] = {
    ensureNotClosed()
    val chunkIndex = itemIndexToChunkIndex(index)
    chunk(chunkIndex) flatMap {(chunk) =>
      val relativeIndex = index - chunkIndex * chunkSize
      chunk.lift(relativeIndex)
    }
  }

  override def loadItem(index: Int): Future[A] = {
    ensureNotClosed()
    val chunkIndex = itemIndexToChunkIndex(index)
    loadChunk(index) map {(chunk) =>
      val relativeIndex = index - chunkIndex * chunkSize
      chunk(relativeIndex)
    }
  }

  override val chunkCount = count / chunkSize + (if ((count % chunkSize) > 0) 1 else 0)
  override def loadedChunkCount: Int = _chunks.count {
    case (_, future) => future.isCompleted
  }

  private def itemIndexToChunkIndex(index: Int): Int = index / chunkSize

  @inline
  private def ensureNotClosed(): Unit = require(!_isClosed, "Cursor already closed")

  private val _chunks = mutable.HashMap[Int, Future[Array[A]]]()
  private var _isClosed = false

}