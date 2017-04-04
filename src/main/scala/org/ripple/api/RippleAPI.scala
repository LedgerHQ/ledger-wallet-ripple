package org.ripple.api

import java.time.LocalDateTime

import org.json.JSONObject
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLIFrameElement

import spray.json._
import DefaultJsonProtocol._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import concurrent.Future
import  concurrent.Promise

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.write



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

//In the future, just cast scala object to json strings instead of using a facade
trait APIOption extends js.Object{
  var server: String = js.native
  var feeCushion: Double = js.native
  var trace: Boolean = js.native
  var proxy: String = js.native
  var timeout: Long = js.native
}

object APIOption {
  def apply() = {
    val dictionary = js.Dictionary[js.Any]()
    dictionary.asInstanceOf[APIOption]
  }
}


class RippleAPI() {
  var promisesTable: Map[String,Int] = Map.empty

  //*************** setOptions *******************
  var setOptionsPromisesTable: Map[String,Promise[SetOptionsResponse]] = Map.empty

  def setOptions(options: APIOption) :Future[SetOptionsResponse] ={
    val p: Promise[SetOptionsResponse] = Promise[SetOptionsResponse]
    val methodId: Int = 0

    val callId: String = LocalDateTime.now.toString() +
      LocalDateTime.now.getSecond.toString + LocalDateTime.now.getNano.toString
    val target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(js.Dynamic.literal(call_id = callId,
      method = "set_option", parameters = options),"*")
    this.setOptionsPromisesTable += (callId->p)
    this.promisesTable += (callId->methodId)
    p.future
  }


  def setOptionsHandler(callId: String, data: JSONObject) = {
    val response = data.asInstanceOf[SetOptionsResponse]
    val p = this.setOptionsPromisesTable.get(callId).get
    this.promisesTable -=  callId
    this.setOptionsPromisesTable -=  callId
    p.success(response)
  }

  class SetOptionsResponse(data:JSONObject) {
    var connected: Boolean = data.getBoolean("connected")
    var error: String = data.getString("error")
  }
  //-----------------------------------------------------
  //****************** classes **********
  class Instructions() {
    var fee: Option[Double] = None
    var maxLedgerVersion: Option[Int] = None
    var maxLedgerVersionOffset: Option[Int] = None
    var sequence: Option[Long] = None
    def this(data: JSONObject) = {
      this()
      if(data.has("fee")){
        this.fee = Some(data.getDouble("fee"))
      }
      if(data.has("maxLedgerVersion")){
        this.maxLedgerVersion = Some(data.getInt("maxLedgerVersion"))
      }
      if(data.has("maxLedgerVersionOffset")){
        this.maxLedgerVersionOffset = Some(data.getInt("maxLedgerVersionOffset"))
      }
      if(data.has("sequence")){
        this.sequence = Some(data.getLong("sequence"))
      }
    }
  }

  class Source(var address: String, var amount: LaxAmount, var tag: Option[Int],
              var maxAmount: LaxAmount) {
  }

  class LaxAmount(var currency: String, var counterparty: Option[String],
                  var value: Option[String]) {

  }

  class Destination(var address: String, var amount: LaxAmount, var tag: Option[Int],
                   var minAmount: LaxAmount) {

  }

  class Payment(var source: Source, var destination: Destination,
                var allowPartialPayment: Option[Boolean], var invoiceID: Option[String],
                var limitQuality: Option[Boolean], var memos: Option[Array[Memo]],
                var noDirectRipple: Option[Boolean], var paths: Option[String]) {
  }

  class Memo(var data: Option[String], var format: Option[String], var `type`: Option[String]) {

  }

  //************** Universal "prepare" methods ********
  var preparePromisesTable: Map[String,Promise[PrepareResponse]] = Map.empty

  def preparePayment(address: String, payment: Payment, instructions: Instructions): Future[PrepareResponse] = {
    val p: Promise[PrepareResponse] = Promise[PrepareResponse] ()
    val methodId: Int = 1
    val methodName: String = "preparePayment"
    val paymentParam: PaymentParam = new PaymentParam(address, payment, instructions)
    this.promisesTable += (getCallId()->methodId)
    this.preparePromisesTable += (getCallId()->p)
    this.messageSender(methodName, paymentParam)
    p.future
  }

  class PaymentParam(val address: String, val payment: Payment,
                     val instructions: Instructions)

  class UniversalPrepareResponse(var success: Boolean, var response: PrepareResponse){
    def this(data: JSONObject) ={
      this(data.getBoolean("success"), new PrepareResponse(data.getJSONObject("response")))
    }
  }

  class PrepareResponse(var txJSON: String, var instructions: Instructions) {
    def this(data: JSONObject) = {
      this(data.getString("txJSON"), new Instructions(data.getJSONObject("instructions")))
    }
  }


  def universalPrepareHandler(callId: String, data: dom.MessageEvent) = {
    val response: UniversalPrepareResponse = new UniversalPrepareResponse(data.data.asInstanceOf[JSONObject])
    val p = this.preparePromisesTable.get(callId).get
    this.promisesTable -=  callId
    this.preparePromisesTable -=  callId
    p.success(response.response)
  }
  //----------------

  // *************** general tools **************

  class MessageToJs(var parameters: Any, var methodName: String, var callId: String)

  def jsonify(message: MessageToJs): String ={
    /* Choose a library play / liftweb/ json4s*/
    implicit val formats = DefaultFormats

    write(message)
  }

  def getCallId = {() =>LocalDateTime.now.toString() +
    LocalDateTime.now.getSecond.toString + LocalDateTime.now.getNano.toString}

  def messageSender(methodName: String, parameters: Any) ={
    val callId: String = getCallId()
    val message = new MessageToJs(parameters, methodName, callId)
    val options = this.jsonify(message)
    val target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(options,"*")
  }

  def onMessage(msg: dom.MessageEvent): Unit = {
    val callId: String = msg.data.asInstanceOf[JSONObject].getString("callID")
    //try
    var methodId: Int = this.promisesTable.get(callId).get
    methodId match {
      case 0 => this.setOptionsHandler(callId, msg.data.asInstanceOf[JSONObject].getJSONObject("response"))
      case 1 => this.universalPrepareHandler(callId, msg) //universal handler for success only method
    }
  }
  dom.document.addEventListener("message", { (e: dom.MessageEvent) => this.onMessage(e)}) //can't figure out how to pass onMessage to the event listener
}