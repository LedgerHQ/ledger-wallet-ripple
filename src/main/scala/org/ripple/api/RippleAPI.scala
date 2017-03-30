package org.ripple.api

import java.time.LocalDateTime

import org.json.JSONObject
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLIFrameElement

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import concurrent.Future
import  concurrent.Promise

/**
  *
  * RippleAPI
  * ledger-wallet-ripple-chrome
  *
  * Created by Alix Mougel on 3/27/17..
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

@js.native
trait APIOption extends js.Object{
  var server: String = js.native
  var feeCushion: Double = js.native
  var trace: Boolean = js.native
  var proxy: String = js.native
  var timeout: Long = js.native
}
@js.native
object APIOption {
  def apply() = {
    val dictionary = js.Dictionary[js.Any]()
    dictionary.asInstanceOf[APIOption]
  }
}


class RippleAPI() {
  var options: APIOption = js.native
  var promisesTable: Map[String,Tuple2[Int,Promise[Any]]] = Map.empty
  var target = js.native


  //*************** setOptions *******************
  def setOptions(options: APIOption) :Promise[Boolean] ={
    val p: Promise[Boolean] = Promise[Boolean]
    val method_id: Int = 1

    val call_id: String = LocalDateTime.now.toString() + LocalDateTime.now.getSecond.toString + LocalDateTime.now.getNano.toString
    this.options = options
    var target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(js.Dynamic.literal(call_id = call_id,method = "set_option", parameters = options),"*")
    this.promisesTable += (call_id->Tuple2(method_id,p))
    return p
  }

  def setOptionsHandler(p: Promise[Any], data: JSONObject) = {
    //val response: SetOptionsResponse = data.parseJson.convertTo[SetOptionsResponse]
    //asinstanceof
    val response = data.asInstanceOf[SetOptionsResponse]
    if(response.connected){
      p.success(true)
    }else{
      p.failure(Exception)
    }
  }

  @js.native
  trait SetOptionsResponse {
    var connected: Boolean = js.native
    var error: String = js.native
  }
  //-----------------------------------------------------

  def onMessage(msg: dom.MessageEvent): Unit = {
    val data: JSONObject = msg.data.asInstanceOf[JSONObject]
    val call_id = data.getString("call_id")
    //try
    var method_id: Int = this.promisesTable.get(call_id).get._1
    var p: Promise[Any] = this.promisesTable.get(call_id).get._2
    method_id match {
      case 1 => this.setOptionsHandler(p, data.getJSONObject("response"))
    }

  }
  dom.document.addEventListener("message", { (e: dom.MessageEvent) => this.onMessage(e)}) //can't figure out how to pass onMessage to the event listener

}