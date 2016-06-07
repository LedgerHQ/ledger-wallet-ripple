package co.ledger.wallet.web.ethereum.core.database

import co.ledger.wallet.web.ethereum.core.idb.{DatabaseConnection, Transaction}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

/**
  *
  * QueryHelper
  * ledger-wallet-ethereum-chrome
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
trait QueryHelper[M <: Model] {
  def database: DatabaseDeclaration
  def creator: ModelCreator[M]
  def newInstance(): M

  def readonly(): ReadOnlyQueryBuilder = new ReadOnlyQueryBuilder
  def readwrite(): ReadWriteQueryBuilder = new ReadWriteQueryBuilder

  trait QueryBuilder {

    def commit(): Future[Unit] = {
      _steps.commit().map((_) => ())
    }

    def :+(perform: PerformStep) = _steps = new QueryStep(_steps, perform)
    private var _steps: QueryStep = new QueryStep(null, (_) => Future.successful(js.Dictionary()))
  }

  class ReadOnlyQueryBuilder extends QueryBuilder {

  }

  class ReadWriteQueryBuilder extends ReadOnlyQueryBuilder {
    def add(item: M): this.type = {
      this
    }

    def add(items: Array[M]): this.type  = {
      for (item <- items)
        add(item)
      this
    }
  }

  private class QueryStep(parent: QueryStep,  perform: PerformStep) {

    def commit(): Future[Any] = {
      null
    }
  }

  type PerformStep = (Transaction) => Future[js.Dictionary[String]]
}

