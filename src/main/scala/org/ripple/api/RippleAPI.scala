package org.ripple.api

import java.time.LocalDateTime

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLIFrameElement

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Promise


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
  var promisesTable: Array[Promise[Any]] = Array[Promise[Any]]()
  var target = js.native
  def setOptions(options: APIOption) :Promise[Any] ={
    val p = Promise[Any]
    val call_id :String = "setOptions_"+LocalDateTime.now.toString()
    this.options = options
    var target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(js.Dynamic.literal(call_id = call_id,method = "set_option", parameters = options),"*")
    this.promisesTable:+p
    p
  }




  def send(message: js.Any): Unit ={

  }
}