package co.ledger.wallet.web.ethereum.core.database

import java.rmi.activation.ActivationGroup_Stub

import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.web.ethereum.core.sjcl.SjclAesCipher
import co.ledger.wallet.web.ethereum.core.webcrypto.WebCryptoCipher
import org.scalajs.dom.crypto.GlobalCrypto

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  * Model
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
class Model(val entityName: String) {
  private val _structure = scala.collection.mutable.Map[String, Value[_]]()
  def structure = _structure.toMap
  protected def int(key: String): Value[Int] = new IntValue(key)
  protected def string(key: String): Value[String] = new StringValue(key)
  protected def boolean(key: String): Value[Boolean] = new BooleanValue(key)
  protected def double(key: String): Value[Double] = new DoubleValue(key)
  protected def long(key: String): Value[Long] = new LongValue(key)
  protected def date(key: String): Value[js.Date] = new DateValue(key)
  protected def index(keys: String*): Unit = _indexes.append(Index(keys.mkString(", "), keys))
  def indexes = _indexes.toArray

  def toDictionary(password: Option[String] = None): js.Dictionary[js.Any] = {
    val dictionary = js.Dictionary[js.Any]()
    val cipher = password.map(new SjclAesCipher(_))
    for (field <- structure.toSeq.map(_._2) if field().isDefined) {
      dictionary(field.key) = field match {
        case f: IntValue => f().get
        case f: StringValue =>
          if (f.isEncrypted && cipher.isDefined) {
            s"encrypted:${cipher.get.encrypt(f().get)}"
          } else {
            f().get
          }
        case f: DoubleValue => f().get
        case f: BooleanValue => f().get
        case f: DateValue => f().get
        case f: LongValue => f().get
      }
    }
    dictionary
  }

  def isEncrypted: Boolean = {
    structure exists {
      case (key, value: StringValue) =>
        value().isDefined && value().get.startsWith("encrypted:")
      case others =>
        false
    }
  }

  private val _indexes = ArrayBuffer[Index]()

  case class Index(name: String, keys: Seq[String])

  class Value[A <: Any](val key: String) {
    _structure(key) = this
    def set(v: A) = {
      _value = Option(v)
      Model.this
    }

    def setWith(v: Option[A]) = {
      _value = v
      Model.this
    }

    def isAutoincrement = _autoincrement
    def autoincrement() = {
      _autoincrement = true
      this
    }
    private var _autoincrement = false

    def clear() = {
      _value = None
      Model.this
    }
    def apply() = _value

    def isUnique = _unique
    def unique() = {
      _unique = true
      this
    }
    private var _unique = false

    def isEncrypted = _encrypted
    def encrypted() = {
      _encrypted = true
      this
    }
    private var _encrypted = false

    def hasIndex = _index.isDefined
    def index(indexName: String = key) = {
      _index = Option(indexName)
      _indexes.append(Index(indexName, Array(key)))
      this
    }
    def index = _index
    private var _index: Option[String] = None

    private var _value: Option[A] = None
  }

  class IntValue(key: String) extends Value[Int](key)
  class StringValue(key: String) extends Value[String](key)
  class BooleanValue(key: String) extends Value[Boolean](key)
  class DoubleValue(key: String) extends Value[Double](key)
  class LongValue(key: String) extends Value[Long](key)
  class DateValue(key: String) extends Value[js.Date](key)
}
