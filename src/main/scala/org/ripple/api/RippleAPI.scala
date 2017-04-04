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

  @js.native
  trait SetOptionsResponse extends js.Object {
    var connected: Boolean = js.native
    var error: String = js.native
  }

  object SetOptionsResponse {
    def apply(connected: Boolean = js.native, error: String = js.native) = {
      var dict = js.Dictionary[js.Any]("connected" -> connected, "error" -> error)
      dict.asInstanceOf[SetOptionsResponse]
    }
  }
  //-----------------------------------------------------
  //****************** classes **********
  @js.native
  trait APIOption extends js.Object{
    var server: String = js.native
    var feeCushion: Double = js.native
    var trace: Boolean = js.native
    var proxy: String = js.native
    var timeout: Long = js.native
  }

  object APIOption {
    def apply(server: String = js.native, feeCushion: Double = js.native,
             trace: Boolean = js.native, proxy: String = js.native, timeout: Long = js.native) = {
      var dictionary = js.Dictionary[js.Any]("server" -> server, "feeCushion" -> feeCushion,
      "trace" -> trace, "proxy" -> proxy, "timeout" -> timeout)
      dictionary.asInstanceOf[APIOption]
    }
  }


  @js.native
  trait Instructions extends js.Object {
    var fee: Double = js.native
    var maxLedgerVersion: Int = js.native
    var maxLedgerVersionOffset: Int = js.native
    var sequence: Long = js.native
    }

  object Instructions {
    def apply(fee: Double = js.native, maxLedgerVersion: Int = js.native,
              maxLedgerVersionOffset: Int = js.native, sequence: Long = js.native) = {
      var dictionary = js.Dictionary[js.Any]("fee" -> fee, "maxLedgerVersion" -> maxLedgerVersion,
      "maxLedgerVersionOffset" -> maxLedgerVersionOffset, "sequence" -> sequence)
      dictionary.asInstanceOf[Instructions]
    }
  }

  trait Source extends js.Object {
    var address: String =js.native
    var amount: LaxAmount = js.native
    var tag: Int = js.native
    var maxAmount: LaxAmount = js.native
  }

  object Source {
    def apply(address: String = js.native, amount: LaxAmount = js.native,
              tag: Int = js.native, maxAmount: LaxAmount = js.native) = {
      var dictionary = js.Dictionary[js.Any]("address" -> address, "amount" -> amount,
        "tag" -> tag, "maxAmount" -> maxAmount)
      dictionary.asInstanceOf[Source]
    }
  }

  @js.native
  trait LaxAmount extends js.Object{
    var currency: String =js.native
    var counterparty: String = js.native
    var value: String = js.native
  }

  object LaxAmount {
    def apply(currency: String = js.native, counterparty: LaxAmount = js.native,
              value: Int = js.native) = {
      var dictionary = js.Dictionary[js.Any]("currency" -> currency, "counterparty" -> counterparty,
        "value" -> value)
      dictionary.asInstanceOf[LaxAmount]
    }
  }

  @js.native
  trait Destination extends js.Object {
    var address: String =js.native
    var amount: String = js.native
    var tag: String = js.native
    var minAmount: String = js.native
  }

  object Destination {
    def apply(address: String = js.native, amount: String = js.native, tag: String = js.native,
              minAmount: String = js.native) = {
      var dictionary = js.Dictionary[js.Any]("address" -> address, "amount" -> amount,
        "minAmount" -> minAmount, "tag" -> tag)
      dictionary.asInstanceOf[Destination]
    }
  }

  @js.native
  trait Payment extends js.Object{
    var source: Source = js.native
    var destination: Destination = js.native
    var allowPartialPayment: Boolean = js.native
    var invoiceID: String = js.native
    var limitQuality: Boolean = js.native
    var memos: Array[Memo] = js.native
    var noDirectRipple: Boolean = js.native
    var paths: String = js.native
  }

  object Payment {
    def apply(source: Source = js.native, destination: Destination = js.native,
              allowPartialPayment: Boolean = js.native,
              invoiceID: String = js.native, limitQuality: Boolean = js.native,
              memos: js.Array[Memo] = js.native,
              noDirectRipple: Boolean = js.native, paths: String = js.native) = {
      var dictionary = js.Dictionary[js.Any]("source" -> source,
        "destination" -> destination, "allowPartialPayment" -> allowPartialPayment,
        "invoiceID" -> invoiceID, "limitQuality" -> limitQuality,
        "memos" -> memos, "noDirectRipple" -> noDirectRipple, "paths" -> paths)
      dictionary.asInstanceOf[Payment]
    }
  }

  @js.native
  trait Memo extends js.Object {
    var data: String = js.native
    var format: String = js.native
    var `type`: String = js.native
  }

  object Memo {
    def apply(data: String = js.native, format: String = js.native,
              `type`: String = js.native) = {
      var dictionary = js.Dictionary[js.Any]("data" -> data, "format" -> format,
        "`type`" -> `type`)
      dictionary.asInstanceOf[Memo]
    }
  }

  //************** Universal "prepare" methods ********
  var preparePromisesTable: Map[String,Promise[PrepareResponse]] = Map.empty

  def preparePayment(address: String, payment: Payment, instructions: Instructions): Future[PrepareResponse] = {
    val p: Promise[PrepareResponse] = Promise[PrepareResponse] ()
    val methodId: Int = 1
    val methodName: String = "preparePayment"
    val paymentParam: PaymentParam = PaymentParam(address, payment, instructions)
    this.promisesTable += (getCallId()->methodId)
    this.preparePromisesTable += (getCallId()->p)
    this.messageSender(methodName, paymentParam)
    p.future
  }

  @js.native
  trait PaymentParam extends js.Object {
    var address: String = js.native
    var payment: Payment = js.native
    var instructions: Instructions = js.native
  }

  object PaymentParam {
    def apply(address: String = js.native, payment: Payment = js.native,
              instructions: Instructions = js.native) = {
      var dictionary = js.Dictionary[js.Any]("address" -> address, "payment" -> payment,
        "instructions" -> instructions)
      dictionary.asInstanceOf[PaymentParam]
    }
  }

  @js.native
  trait UniversalPrepareResponse extends js.Object {
    var success: Boolean = js.native
    var response: PrepareResponse = js.native
  }

  object UniversalPrepareResponse {
    def apply(success: Boolean = js.native, response: PrepareResponse = js.native) = {
      var dictionary = js.Dictionary[js.Any]("success" -> success, "response" -> response)
      dictionary.asInstanceOf[UniversalPrepareResponse]
    }
  }

  @js.native
  trait PrepareResponse extends js.Object {
    var txJSON: String = js.native
    var instructions: Instructions = js.native
  }

  object PrepareResponse {
    def apply(txJSON: String = js.native, instructions: Instructions = js.native) = {
      var dictionary = js.Dictionary[js.Any]("txJSON" -> txJSON, "instructions" -> instructions)
      dictionary.asInstanceOf[PrepareResponse]
    }
  }

  def universalPrepareHandler(callId: String, data: dom.MessageEvent) = {
    val response: UniversalPrepareResponse = data.data.asInstanceOf[UniversalPrepareResponse]
    val p = this.preparePromisesTable.get(callId).get
    this.promisesTable -=  callId
    this.preparePromisesTable -=  callId
    p.success(response.response)
  }
  //----------------

  // *************** general tools **************

  @js.native
  trait MessageToJs extends js.Object {
  var parameters: js.Any = js.native
  var methodName: String = js.native
  var callId: String = js.native
}

object MessageToJs {
  def apply(parameters: js.Any = js.native, methodName: String = js.native,
           callId: String = js.native) = {
    var dictionary = js.Dictionary[js.Any]("parameters" -> parameters,
      "methodName" -> methodName, "callId" -> callId)
    dictionary.asInstanceOf[MessageToJs]
  }
}



  def jsonify(message: MessageToJs): String ={
    //Serialize a js.object here
    return "test"
  }

  def getCallId = {() =>LocalDateTime.now.toString() +
    LocalDateTime.now.getSecond.toString + LocalDateTime.now.getNano.toString}

  def messageSender(methodName: String, parameters: js.Any) ={
    val callId: String = getCallId()
    val message = MessageToJs(parameters, methodName, callId)
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