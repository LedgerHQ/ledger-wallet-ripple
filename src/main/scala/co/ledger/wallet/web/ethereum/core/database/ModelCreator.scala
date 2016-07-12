package co.ledger.wallet.web.ethereum.core.database

import co.ledger.wallet.web.ethereum.core.sjcl.SjclAesCipher
import org.scalajs.dom.idb

import scala.scalajs.js

/**
  *
  * ModelCreator
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
trait ModelCreator[M <: Model] {

  def create(database: idb.Database, transaction: idb.Transaction): Unit = {
    val m = newInstance()
    val keyPath = m.structure.find({
      case (key, value) => value.isUnique || value.isAutoincrement
    }).map(_._2)
    val store = {
      if (!database.objectStoreNames.contains(m.entityName)) {
        keyPath match {
          case Some(k) =>
            if (!k.isAutoincrement)
              database.createObjectStore(m.entityName, js.Dictionary("keyPath" -> k.key))
            else
              database.createObjectStore(m.entityName, js.Dictionary("keyPath" -> k.key, "autoIncrement" -> true))
          case None =>
            database.createObjectStore(m.entityName, js.Dictionary("autoIncrement" -> true))
        }
      } else {
        transaction.objectStore(m.entityName)
      }
    }
    for (index <- m.indexes) {
      if (!store.indexNames.contains(index.name)) {
        store.asInstanceOf[js.Dynamic].createIndex(index.name, js.Array(index.keys:_*))
      }
    }
  }

  def apply(map: js.Dictionary[js.Any], password: Option[String]): M = {
    val result = newInstance()
    val cipher = password.map(new SjclAesCipher(_))
    for (field <- result.structure.toSeq.map(_._2) if map.contains(field.key)) {
      field match {
        case f: result.IntValue =>
          f.set(map(f.key).asInstanceOf[Int])
        case f: result.StringValue =>
          val value = map(f.key).asInstanceOf[String]
          if (cipher.isDefined && value.startsWith("encrypted:")) {
            f.set(cipher.get.decrypt(value.substring(10)))
          } else {
            f.set(value)
          }
        case f: result.DoubleValue =>
          f.set(map(f.key).asInstanceOf[Double])
        case f: result.BooleanValue =>
          f.set(map(f.key).asInstanceOf[Boolean])
        case f: result.LongValue =>
          f.set(map(f.key).asInstanceOf[Double].toLong)
        case f: result.DateValue =>
          f.set(map(f.key).asInstanceOf[js.Date])
      }
    }
    result
  }

  def newInstance(): M

}
