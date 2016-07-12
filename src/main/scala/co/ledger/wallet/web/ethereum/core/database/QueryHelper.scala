package co.ledger.wallet.web.ethereum.core.database

import co.ledger.wallet.web.ethereum.core.sjcl.SjclAesCipher
import co.ledger.wallet.web.ethereum.services.SessionService
import org.scalajs.dom.{ErrorEvent, Event, idb}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Future, Promise}
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
abstract class QueryHelper[M >: Null <: Model](implicit classTag: ClassTag[M]) {
  private val modelDeclaration = newInstance()
  def database: DatabaseDeclaration
  def creator: ModelCreator[M]
  def newInstance(): M

  def readonly(password: Option[String]): ReadOnlyQueryBuilder = new ReadOnlyQueryBuilder(password)
  def readwrite(password: Option[String]): ReadWriteQueryBuilder = new ReadWriteQueryBuilder(password)

  trait QueryBuilder {
    protected def mode: String
    def commit(): Future[QueryResult] = {
      _steps.commit(mode)
    }
    protected val password: Option[String]
    def cursor: Future[Cursor[M]] = openCursor().commit().map(_.cursor)
    def items: Future[Array[M]] = commit().map(_.items)
    def openCursor(keys: String*): this.type
    def count: Future[Long]
    protected def :+(perform: PerformStep) = _steps = new QueryStep(_steps, perform)
    private var _steps: QueryStep = new QueryStep(null, (_, _) => Future.successful())
  }

  class ReadOnlyQueryBuilder(protected override val password: Option[String]) extends QueryBuilder with CursorBuilder[M] {
    override protected implicit val modelClassTag: ClassTag[M] = classTag
    override protected val modelDeclaration: M = QueryHelper.this.modelDeclaration
    override protected def mode: String = "readonly"
    private lazy val cipher = new SjclAesCipher(password.get)
    override protected val creator: ModelCreator[M] = QueryHelper.this.creator

    def get(primaryKey: js.Any): this.type = {
      this :+ {(transaction, result) =>
        val promise = Promise[Unit]()
        val objectStore = transaction.objectStore(modelDeclaration.entityName)
        val k: js.Any = {
          if (modelDeclaration.structure.find(_._2.isUnique).get._2.isEncrypted) {
            s"encrypted:${cipher.encrypt(primaryKey.asInstanceOf[String])}"
          } else {
            primaryKey
          }
        }
        val request = objectStore.get(k)
        request.onerror = {(event: ErrorEvent) =>
          promise.failure(new Exception(event.message))
        }
        request.onsuccess = {(event: Event) =>
          if (!js.isUndefined(event.asInstanceOf[js.Dynamic].target.result.asInstanceOf[js.Dictionary[js.Any]])) {
            val r = creator(event.asInstanceOf[js.Dynamic].target.result.asInstanceOf[js.Dictionary[js.Any]], password)
            result.addItem(r)
          }
          promise.success()
        }
        promise.future
      }
      this
    }

    override def openCursor(keys: String*): this.type = {
      this :+ {(transaction, result) =>
        if (result.cursor == null) {
          indexName = if (keys.isEmpty) None else Some(keys.mkString(","))
          buildCursor(transaction) map { (c) =>
            result.setCursor(c)
            ()
          }
        } else {
          Future.successful()
        }
      }
      this
    }

    override def count: Future[Long] = {
      val promise = Promise[Long]()
      this :+ {(transaction, result) =>
        val f = count(transaction)
        promise.completeWith(f)
        f.map(_ => ())
      }
      commit()
      promise.future
    }

    def toArray: Future[Array[M]] = {
      cursor flatMap {(c) =>
        c.toArray
      }
    }

  }

  class ReadWriteQueryBuilder(pwd: Option[String]) extends ReadOnlyQueryBuilder(pwd) {
    override protected def mode: String = "readwrite"
    def add(item: M): this.type = {
      this :+ {(transaction, result) =>
        val promise = Promise[Unit]()
        try {
          val request = transaction.objectStore(modelDeclaration.entityName).add(item.toDictionary(password))
          request.onerror = { (event: ErrorEvent) =>
            promise.failure(new Exception(event.asInstanceOf[js.Dynamic].target.error.message.asInstanceOf[String]))
          }
          request.onsuccess = { (event: Event) =>
            promise.success()
          }
        } catch {
          case er: Throwable => promise.failure(er)
        }
        promise.future
      }
      this
    }

    def writeCursor: Future[WriteCursor[M]] = commit().map(_.cursor.asInstanceOf[WriteCursor[M]])
    def add(items: Array[M]): this.type  = {
      for (item <- items)
        add(item)
      this
    }

    def deleteAll(predicate: (M) => Boolean): Future[Int] = {
      writeCursor flatMap {(cursor) =>
        cursor.deleteAll(predicate)
      }
    }

    useWriteCursor()
  }

  private class QueryStep(val parent: QueryStep, val perform: PerformStep) {
    var child: QueryStep = null

    if (parent != null) {
      parent.child = this
    }

    def root: QueryStep = {
      var n = this
      while (n.parent != null)
        n = n.parent
      n
    }

    def commit(mode: String): Future[QueryResult] = {
      val r = root
      val result = new MutableQueryResult
      database.obtainConnection() flatMap {(connection) =>
        val transaction = connection.transaction(js.Array(modelDeclaration.entityName), mode)
        def iterate(step: QueryStep): Future[Unit] = {
          if (step == null)
            Future.successful()
          else
            step.perform(transaction, result) flatMap {(_) =>
              iterate(step.child)
            }
        }
        iterate(r)
      } map((_) => result.asInstanceOf[QueryResult])
    }
  }

  trait QueryResult {
    def cursor: Cursor[M]
    def items: Array[M]
  }

  private class MutableQueryResult extends QueryResult {
    override def cursor: Cursor[M] = _cursor
    override def items: Array[M] = _items.toArray
    def addItem(item: M) = _items.append(item)
    def setCursor(cursor: Cursor[M]): Unit = _cursor = cursor
    private val _items = ArrayBuffer[M]()
    private var _cursor: Cursor[M] = null
  }

  private type PerformStep = (idb.Transaction, MutableQueryResult) => Future[Unit]
}
