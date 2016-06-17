package org.json

import scala.scalajs.js
import scala.scalajs.js.JSON

/**
  *
  * JSONObject
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 16/06/2016.
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
// Minimalistic version of the Android JSON library
class JSONObject(private[json] val obj: js.Dynamic) {

  def this(json: String) = this(JSON.parse(json))
  def this() = this(js.Dynamic.newInstance(js.Dynamic.global.Object)())

  def length() = obj.length.asInstanceOf[Int]

  def put(name: String, value: Boolean) = {
    obj.updateDynamic(name)(value)
    this
  }

  def put(name: String, value: Int) = {
    obj.updateDynamic(name)(value)
    this
  }

  def put(name: String, value: Long) = {
    obj.updateDynamic(name)(value)
    this
  }

  def put(name: String, value: Double) = {
    obj.updateDynamic(name)(value)
    this
  }

  def put(name: String, value: String) = {
    obj.updateDynamic(name)(value)
    this
  }

  def put(name: String, value: JSONObject) = {
    obj.updateDynamic(name)(value.obj)
    this
  }

  def put(name: String, value: JSONArray) = {
    obj.updateDynamic(name)(value.array)
    this
  }

  def has(name: String) = obj.selectDynamic(name) != null

  def optBoolean(name: String, fallBack: Boolean): Boolean = Option(obj.selectDynamic(name).asInstanceOf[Boolean]).getOrElse(fallBack)
  def optInt(name: String, fallBack: Int): Int = Option(obj.selectDynamic(name).asInstanceOf[Int]).getOrElse(fallBack)
  def optLong(name: String, fallBack: Long): Long = Option(obj.selectDynamic(name).asInstanceOf[Long]).getOrElse(fallBack)
  def optDouble(name: String, fallBack: Double) = Option(obj.selectDynamic(name).asInstanceOf[Double]).getOrElse(fallBack)
  def optString(name: String, fallBack: String) = Option(obj.selectDynamic(name).asInstanceOf[String]).getOrElse(fallBack)
  def optJSONObject(name: String, fallBack: JSONObject = null) =
    Option(obj.selectDynamic(name)).map(new JSONObject(_)).getOrElse(fallBack)
  def optJSONArray(name: String, fallBack: JSONArray = null) =
    Option(obj.selectDynamic(name).asInstanceOf[js.Array[js.Any]]).map(new JSONArray(_)).getOrElse(fallBack)

  @throws(classOf[JSONException])
  def getBoolean(name: String) = Option(obj.selectDynamic(name).asInstanceOf[Boolean]).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getInt(name: String) = Option(obj.selectDynamic(name).asInstanceOf[Int]).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getLong(name: String) = Option(obj.selectDynamic(name).asInstanceOf[Double].toLong).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getDouble(name: String) = Option(obj.selectDynamic(name).asInstanceOf[Double]).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getString(name: String) = Option(obj.selectDynamic(name).toString).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getJSONObject(name: String) =
    Option(obj.selectDynamic(name)).map(new JSONObject(_)).getOrElse(throw new JSONException("No value for " + name))
  @throws(classOf[JSONException])
  def getJSONArray(name: String) =
    Option(obj.selectDynamic(name).asInstanceOf[js.Array[js.Any]]).map(new JSONArray(_)).getOrElse(throw new JSONException("No value for " + name))

  def names() = new JSONArray(js.Dynamic.global.Object.keys(obj).asInstanceOf[js.Array[js.Any]])
  def toString(indent: Int) = js.Dynamic.global.JSON.stringify(obj, null, indent).asInstanceOf[String]
  override def toString: String = toString(0)
}
