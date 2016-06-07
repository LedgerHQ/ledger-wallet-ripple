package co.ledger.wallet.web.ethereum.core.database

import java.rmi.activation.ActivationGroup_Stub

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

  def toDictionary: js.Dictionary[js.Any] = {
    val dictionary = js.Dictionary[js.Any]()
    for (field <- structure.toSeq.map(_._2) if field().isDefined) {
      dictionary(field.key) = field match {
        case f: IntValue => f().get
        case f: StringValue => f().get
        case f: DoubleValue => f().get
        case f: BooleanValue => f().get
      }
    }
    dictionary
  }

  class Value[A <: Any](val key: String) {
    _structure(key) = this
    def set(v: A) = {
      _value = Option(v)
      Model.this
    }

    def clear() = {
      _value = None
      Model.this
    }
    def apply() = _value

    def unique() = {
      this
    }

    def index(indexName: String = key) = {
      this
    }

    private var _value: Option[A] = None
  }

  class IntValue(key: String) extends Value[Int](key)
  class StringValue(key: String) extends Value[String](key)
  class BooleanValue(key: String) extends Value[Boolean](key)
  class DoubleValue(key: String) extends Value[Double](key)
  class LongValue(key: String) extends Value[Long](key)

}
