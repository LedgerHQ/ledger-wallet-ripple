package co.ledger.wallet.web.ripple.core.database

import co.ledger.wallet.web.ripple.core.idb.IndexedDb
import com.sun.org.apache.xpath.internal.functions.FuncTranslate
import org.scalajs.dom.idb

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  *
  * DatabaseDeclaration
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 07/06/2016.
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
trait DatabaseDeclaration {
  def name: String
  def version: Int
  def models: Seq[QueryHelper[_]]

  def open(): Future[idb.Database] = {
    IndexedDb.open(name, Some(version)) {(connection, transaction) =>
      // Create all store
      for (model <- models) {
        model.creator.create(connection, transaction)
      }
    } andThen {
      case Success(connection) => _connection = Option(connection)
      case Failure(ex) => ex.printStackTrace()
    }
  }

  def obtainConnection(): Future[idb.Database] = {
    connection match {
      case Some(c) => Future.successful(c)
      case None => open()
    }
  }

  def close(): Unit = _connection.foreach(_.close())
  def connection: Option[idb.Database] = _connection
  def delete() = IndexedDb.delete(name)
  private var _connection: Option[idb.Database] = None
}
