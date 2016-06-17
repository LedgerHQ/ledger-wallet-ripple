package org.json

import scala.scalajs.js
import scala.scalajs.js.JSON

/**
  *
  * JSONArray
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
class JSONArray(private[json] val array: js.Array[js.Any]) {

  def this() = this(js.Array[js.Any]())
  def this(json: String) = this(JSON.parse(json).asInstanceOf[js.Array[js.Any]])

  def length() = array.length

  def put(value: Boolean) = {
    array.push(value)
    this
  }

  def put(value: Int) = {
    array.push(value)
    this
  }

  def put(value: Long) = {
    array.push(value)
    this
  }

  def put(value: String) = {
    array.push(value)
    this
  }

  def put(value: JSONObject) = {
    array.push(value.obj)
    this
  }

  def put(value: JSONArray) = {
    array.push(value.array)
    this
  }

  @throws(classOf[JSONException])
  def getBoolean(index: Int) = array.lift(index).map(_.asInstanceOf[Boolean]).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getInt(index: Int) = array.lift(index).map(_.asInstanceOf[Int]).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getLong(index: Int) = array.lift(index).map(_.asInstanceOf[Long]).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getDouble(index: Int) = array.lift(index).map(_.asInstanceOf[Double]).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getString(index: Int) = array.lift(index).map(_.toString).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getJSONObject(index: Int) = array.lift(index).map({(o) => new JSONObject(o.asInstanceOf[js.Dynamic])}).getOrElse(throw new JSONException(""))
  @throws(classOf[JSONException])
  def getJSONArray(index: Int) = array.lift(index).map({(o) => new JSONArray(o.asInstanceOf[js.Array[js.Any]])}).getOrElse(throw new JSONException(""))

  def toString(indent: Int) = js.Dynamic.global.JSON.stringify(array, null, indent).asInstanceOf[String]
  override def toString: String = toString(0)
}
