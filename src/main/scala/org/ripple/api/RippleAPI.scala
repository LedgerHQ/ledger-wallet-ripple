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

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._



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

  case class APIOption(server: Option[String], feeCushion: Option[Double], trace: Option[Boolean],
                       proxy: Option[String], timeout: Option[Long])

  def setOptions(options: APIOption): Future[SetOptionsResponse] ={
    val p: Promise[SetOptionsResponse] = Promise[SetOptionsResponse]
    val methodId: Int = 0
    val callId = getCallId()
    val methodName = "set_option"
    this.promisesTable += (callId->methodId)
    this.setOptionsPromisesTable += (callId->p)
    this.messageSender(methodName, options)
    p.future
  }


  def setOptionsHandler(callId: String, data: dom.MessageEvent) = {
    val response = decode[SetOptionsResponse](SetOptionsResponse.asJson.spaces4).right.get
    val p = this.setOptionsPromisesTable.get(callId).get
    this.promisesTable -=  callId
    this.setOptionsPromisesTable -=  callId
    p.success(response)
  }

  case class SetOptionsResponse(connected: Boolean, error: Option[String])

  //-----------------------------------------------------
  //****************** classes **********
  case class Instructions(fee: Option[Double] = None,maxLedgerVersion: Option[Int] = None,
                          maxLedgerVersionOffset: Option[Int] = None, sequence: Option[Long] = None)

  case class Source(address: String, amount: LaxAmount, tag: Option[Int], maxAmount: LaxAmount)

  case class LaxAmount(currency: String, counterparty: Option[String], value: Option[String])

  case class Destination(address: String, amount: LaxAmount, tag: Option[Int], minAmount: LaxAmount)

  case class Payment(source: Source, destination: Destination, allowPartialPayment: Option[Boolean],
                     invoiceID: Option[String], limitQuality: Option[Boolean],
                     memos: Option[Array[Memo]], noDirectRipple: Option[Boolean], paths: Option[String])

  case class Memo(data: Option[String], format: Option[String], `type`: Option[String])

  //************** Universal "prepare" methods ********
  var preparePromisesTable: Map[String,Promise[PrepareResponse]] = Map.empty

  def preparePayment(address: String, payment: Payment, instructions: Option[Instructions] = None): Future[PrepareResponse] = {
    val p: Promise[PrepareResponse] = Promise[PrepareResponse] ()
    val methodId: Int = 1
    val methodName: String = "preparePayment"
    val paymentParam: PaymentParam = new PaymentParam(address, payment, instructions)
    val callId = getCallId()
    this.promisesTable += (callId->methodId)
    this.preparePromisesTable += (callId->p)
    this.messageSender(methodName, paymentParam)
    p.future
  }

  case class PaymentParam(address: String, payment: Payment, instructions: Option[Instructions])

  case class UniversalPrepareResponse(success: Boolean, response: PrepareResponse)

  case class PrepareResponse(txJSON: String, instructions: Instructions)

  def universalPrepareHandler(callId: String, data: dom.MessageEvent) = {
    val response: UniversalPrepareResponse = decode[UniversalPrepareResponse](UniversalPrepareResponse.asJson.spaces4).right.get
    val p = this.preparePromisesTable.get(callId).get
    this.promisesTable -=  callId
    this.preparePromisesTable -=  callId
    p.success(response.response)
  }
  //----------------

  // *************** general tools **************

  case class MessageToJs(parameters: Any, methodName: String, callId: String)

  def getCallId = {() =>LocalDateTime.now.toString() +
    LocalDateTime.now.getSecond.toString + LocalDateTime.now.getNano.toString}

  def messageSender(methodName: String, parameters: Any) ={
    val callId: String = getCallId()
    val message = new MessageToJs(parameters, methodName, callId)
    val options = message.asJson.noSpaces
    val target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(options,"*")
  }

  def onMessage(msg: dom.MessageEvent): Unit = {
    val callId: String = msg.data.asInstanceOf[JSONObject].getString("callID")
    //try
    var methodId: Int = this.promisesTable.get(callId).get
    methodId match {
      case 0 => this.setOptionsHandler(callId, msg)
      case 1 => this.universalPrepareHandler(callId, msg) //universal handler for success only method
    }
  }
  dom.document.addEventListener("message", { (e: dom.MessageEvent) => this.onMessage(e)}) //can't figure out how to pass onMessage to the event listener
}