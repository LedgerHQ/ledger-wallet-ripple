package co.ledger.wallet.web.ethereum.core.idb

import org.scalajs.dom.{ErrorEvent, Event, idb}
import org.scalajs.dom.raw.IDBVersionChangeEvent

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  *
  * IndexedDb
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 06/06/2016.
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
object IndexedDb {

  def open(databaseName: String, version: Option[Int] = Some(1))(upgradeHandler: (idb.Database, idb.Transaction) => Unit): Future[idb.Database] = {
    val promise = Promise[idb.Database]()
    val request = version match {
      case Some(v) => factory.open(databaseName, v)
      case None => factory.open(databaseName)
    }
    request.onupgradeneeded = {(event: IDBVersionChangeEvent) =>
      val db = event.target.asInstanceOf[js.Dynamic].result.asInstanceOf[idb.Database]
      val transaction = event.currentTarget.asInstanceOf[js.Dynamic].transaction.asInstanceOf[idb.Transaction]
      upgradeHandler(db, transaction)
    }
    request.onblocked = {(event: Event) =>

    }
    request.onsuccess = {(event: Event) =>
      val db = event.target.asInstanceOf[js.Dynamic].result.asInstanceOf[idb.Database]
      promise.success(db)
    }
    request.onerror = {(event: ErrorEvent) =>
      promise.failure(new Exception(event.message))
    }
    promise.future
  }

  def delete(databaseName: String) = {
    factory.deleteDatabase(databaseName)
  }

  private def factory = js.Dynamic.global.indexedDB.asInstanceOf[idb.Factory]

}
