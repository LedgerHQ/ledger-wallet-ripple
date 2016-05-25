package co.ledger.wallet.web.ethereum.core.utils

import co.ledger.wallet.core.utils.Preferences
import upickle.Js

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  * ChromePreferences
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 25/05/2016.
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
class ChromePreferences(name: String) extends Preferences {
  override def all: Map[String, _] = _data

  override def boolean(key: String): Option[Boolean] = _data.lift(key).map(_.asInstanceOf[Boolean])

  override def stringSet(key: String): Option[Set[String]] = _data.lift(key).map(_.asInstanceOf[Set[String]])

  override def float(key: String): Option[Float] = _data.lift(key).map(_.asInstanceOf[Float])

  override def int(key: String): Option[Int] = _data.lift(key).map(_.asInstanceOf[Int])

  override def string(key: String): Option[String] = _data.lift(key).map(_.asInstanceOf[String])

  override def edit(): Editor = new ChromeEditor()

  override def long(key: String): Option[Long] = _data.lift(key).map(_.asInstanceOf[Long])

  private class ChromeEditor extends this.Editor {

    override def putString(key: String, string: String): Editor = {
      _changes(key) = string
      this
    }

    override def clear(): Editor = {
      _clearAll = true
      this
    }

    override def putFloat(key: String, float: Float): Editor = {
      _changes(key) = float
      this
    }

    override def remove(key: String): Editor = {
      _remove += key
      this
    }

    override def putBoolean(key: String, boolean: Boolean): Editor = {
      _changes(key) = boolean
      this
    }

    override def putInt(key: String, int: Int): Editor = {
      _changes(key) = int
      this
    }

    override def putLong(key: String, long: Long): Editor = {
      _changes(key) = long
      this
    }

    override def putStringSet(key: String, stringSet: Set[String]): Editor = {
      _changes(key) = stringSet
      this
    }

    override def commit(): Unit = {
      // Clear data if necessary
      if (_clearAll) {
        _data = Map[String, Any]()
      }
      val data = scala.collection.mutable.Map[String, Any]()
      // Restore data in mutable state
      _data foreach {
        case (key, value) =>
          data(key) = value
      }
      // Remove keys
      _remove foreach {(key) =>
        data -= key
      }
      // Apply changes
      _changes foreach {
        case (key, value) =>
          data(key) = value
      }
      _data = data.toMap
      ChromePreferences.save(name, _data)
    }

    private val _changes = scala.collection.mutable.Map[String, Any]()
    private val _remove = ArrayBuffer[String]()
    private var _clearAll = false
  }

  private var _data = ChromePreferences.get(name)
}

object ChromePreferences {
  import upickle.default._


  def load(name: String, password: String): Future[Unit] = {
    import js._
    import Dynamic._

    val promise = scala.concurrent.Promise[Unit]()
    val chrome = global.chrome
    chrome.storage.local.get(name, {(result: Dictionary[String]) =>
      global.console.log("Result is ", result)
      val json = if (result.contains(name)) result(name) else "{}"
      read[Map[String, String]](json) foreach {
        case (key, value) =>
          _data(key) = value
      }
      promise.success()
    })
    promise.future
  }

  private[ChromePreferences] def save(name: String, data: Map[String, _]): Unit = {

    def scala2JsonValue(x: Any): Js.Value = {
      x match {
        case true =>
          Js.True
        case false =>
          Js.False
        case null =>
          Js.Null
        case value:Int =>
          Js.Num(value)
        case value:Float =>
          Js.Num(value)
        case value:Double =>
          Js.Num(value)
        case value:Short =>
          Js.Num(value)
        case value: String =>
          Js.Str(value)
      }
    }
    var kvs = Seq[(String, Js.Value)]()
    data foreach {
      case (key, value: Array[Any]) =>
        var array = Seq[Js.Value]()
        for (v <- array)
          array = array :+ scala2JsonValue(v)
        kvs = kvs :+ (key -> Js.Arr(array:_*))
      case (key, value) =>
        kvs = kvs :+ (key -> scala2JsonValue(value))
    }
    val serialized = upickle.json.write(Js.Obj(kvs:_*))
    println("Serialized is " + serialized)
  }

  private[ChromePreferences] def get(name: String): Map[String, _] = {
    if (_data.contains(name)) {
      Map[String, Any]()
    } else {
      Map[String, Any]()
    }
  }

  def close() = {
    _data.clear()
  }

  private val _data = scala.collection.mutable.Map[String, String]()
}