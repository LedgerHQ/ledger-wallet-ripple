package co.ledger.wallet.web.ripple.core.utils

import co.ledger.wallet.core.utils.Preferences
import upickle.Js

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  * ChromePreferences
  * ledger-wallet-ripple-chrome
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
/**
  *
  * @example
  * // Preferences tests
  * ChromePreferences.init() foreach {(_) =>
      ChromePreferences.load("toto", "toto") onComplete {
        case Success(_) =>
        {
          val preferences = new ChromePreferences("Test")
          println(s"Before ${preferences.string("pref")}")
          println(s"Before ${preferences.int("int")}")
          println(s"Before ${preferences.float("float")}")
          preferences.edit()
            .putString("pref", "hey")
            .putInt("int", 12)
            .putFloat("float", 12.5f)
            .commit()
          println(preferences.string("pref").get)
        }
        {
          val preferences = new ChromePreferences("SuperTest")
          println(s"Before ${preferences.string("pref")}")
          println(s"Before ${preferences.int("int")}")
          println(s"Before ${preferences.float("float")}")
          preferences.edit()
            .putString("pref", "hey")
            .putInt("int", 12)
            .putFloat("float", 12.5f)
            .commit()
          println(preferences.string("pref").get)
        }
        case Failure(ex) => ex.printStackTrace()
      }
      val preferences = new ChromeGlobalPreferences("GTest")
      println(s"Global ${preferences.string("pref")}")
      println(s"Global ${preferences.int("int")}")
      println(s"Global ${preferences.float("float")}")
      preferences.edit()
        .putString("pref", "hey")
        .putInt("int", 12)
        .putFloat("float", 12.5f)
        .commit()
      println(preferences.string("pref").get)
    }
 */
class ChromePreferences(name: String) extends Preferences {
  override def all: Map[String, _] = _data

  override def boolean(key: String): Option[Boolean] = _data.lift(key).map(_.asInstanceOf[Boolean])

  override def stringSet(key: String): Option[Set[String]] = _data.lift(key).map(_.asInstanceOf[Set[String]])

  override def float(key: String): Option[Float] = _data.lift(key).map(_.asInstanceOf[Float])

  override def int(key: String): Option[Int] = _data.lift(key).map(_.asInstanceOf[Double].toInt)

  override def string(key: String): Option[String] = _data.lift(key).map(_.asInstanceOf[String])

  override def edit(): Editor = new ChromeEditor()

  override def long(key: String): Option[Long] = _data.lift(key).map(_.asInstanceOf[Double].toLong)

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
      ChromePreferences.save(name, _data, isGlobal)
    }

    private val _changes = scala.collection.mutable.Map[String, Any]()
    private val _remove = ArrayBuffer[String]()
    private var _clearAll = false
  }

  private var _data = ChromePreferences.get(name, isGlobal)

  def isGlobal = false
}

class ChromeGlobalPreferences(name: String) extends ChromePreferences(name) {
  override def isGlobal: Boolean = true
}

object ChromePreferences {
  import upickle.default._

  val GlobalStoreName = "global"

  def init(): Future[Unit] = load(GlobalStoreName, "", _globals)

  def load(name: String, password: String): Future[Unit] = load(name, password, _data)

  private def load(name: String, password: String, map: scala.collection.mutable.Map[String, String]): Future[Unit] = {
    import js._
    import Dynamic._

    _currentStoreName = name
    val promise = scala.concurrent.Promise[Unit]()
    val chrome = global.chrome
    chrome.storage.local.get(name, {(result: Dictionary[String]) =>
      val json = if (result.contains(name)) result(name) else "{}"
      read[Map[String, String]](json) foreach {
        case (key, value) =>
          map(key) = value
      }
      promise.success()
    })
    promise.future
  }

  private[ChromePreferences] def save(name: String, data: Map[String, _], global: Boolean): Unit = {
    val d = if (global) _globals else _data
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
        case value:Long =>
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
    // Time for encryption
    val encrypted = serialized
    // Storage time
    d(name) = encrypted
    save(global)
  }

  private def save(isGlobal: Boolean): Unit = {
    import js._
    import Dynamic._
    val chrome = global.chrome

    val name = if (isGlobal) GlobalStoreName else _currentStoreName
    val data = if (isGlobal) _globals else _data
    var kvs = Seq[(String, Js.Value)]()
    data foreach {
      case (key, value) =>
        kvs = kvs :+ (key -> Js.Str(value))
    }
    val serialized = upickle.json.write(Js.Obj(kvs:_*))
    chrome.storage.local.set(js.Dictionary[String](
      name -> serialized
    ))
  }

  private[ChromePreferences] def get(name: String, global: Boolean): Map[String, _] = {
    val data = if (global) _globals else _data
    if (data.contains(name)) {
      upickle.json.read(data(name)).obj map {
        case (key, value: Js.Str) =>
          (key, value.value)
        case (key, value: Js.Num) =>
          (key, value.value)
        case (key, value: Js.Obj) =>
          (key, "")
        case (key, Js.False) =>
          (key, false)
        case (key, Js.True) =>
          (key, true)
        case (key, Js.Null) =>
          (key, null)
        case (key, value: Js.Arr) =>
          val array = value.value map {
            case Js.Str(v) => v
            case other => ""
          }
          (key, array)
      }
    } else {
      Map[String, Any]()
    }
  }

  def close() = {
    _data.clear()
  }

  private val _data = scala.collection.mutable.Map[String, String]()
  private val _globals = scala.collection.mutable.Map[String, String]()
  private var _currentStoreName = ""
}