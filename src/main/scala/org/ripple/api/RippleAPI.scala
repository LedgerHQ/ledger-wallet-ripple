package org.ripple.api

import java.time.LocalTime
import org.json.JSONObject
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLIFrameElement

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import concurrent.Future
import concurrent.Promise
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.Printer
import io.circe.syntax._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import io.circe.generic.JsonCodec, io.circe.syntax._

import io.circe._, io.circe.generic.semiauto._
import co.ledger.wallet.core.utils.Nullable

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

sealed trait RippleAPIObject

case class APIOption(
                      server: Option[String] = None,
                      feeCushion: Option[Double] = None,
                      trace: Option[Boolean] = None,
                      proxy: Option[Nullable[String]] = Some(Nullable[String](None)),
                      timeout: Option[Long] = None
                    ) extends RippleAPIObject

class RippleAPI() {

  var promisesTable: Map[Int,Promise[String]] = Map.empty
  def disconnect(): Future[SetOptionsResponse]  = {
    val methodName = "disconnect"
    execute(methodName, APIOption()).map(decode[SetOptionsResponse](_).right.get)
  }

  //*************** setOptions *******************
  var setOptionsPromisesTable: Map[String,Promise[SetOptionsResponse]] = Map.empty



  def setOptions(options: APIOption): Future[SetOptionsResponse] ={
    val methodName = "setOption"
    execute(methodName, options).map(decode[SetOptionsResponse](_).right.get)
  }

  case class SetOptionsResponse(connected: Boolean, error: Option[String]) extends RippleAPIObject

  //-----------------------------------------------------
  //****************** classes **********
  case class Instructions(fee: Option[Double] = None,maxLedgerVersion: Option[Int] = None,
                          maxLedgerVersionOffset: Option[Int] = None, sequence: Option[Long] = None) extends RippleAPIObject

  case class Source(address: String, amount: LaxAmount, tag: Option[Int], maxAmount: LaxAmount) extends RippleAPIObject

  case class LaxAmount(currency: String, counterparty: Option[String], value: Option[String]) extends RippleAPIObject

  case class Destination(address: String, amount: LaxAmount, tag: Option[Int], minAmount: LaxAmount) extends RippleAPIObject

  case class Payment(source: Source, destination: Destination, allowPartialPayment: Option[Boolean],
                     invoiceID: Option[String], limitQuality: Option[Boolean],
                     memos: Option[Array[Memo]], noDirectRipple: Option[Boolean], paths: Option[String]) extends RippleAPIObject

  case class Memo(data: Option[String], format: Option[String], `type`: Option[String]) extends RippleAPIObject
  //--------------------
  //************** Universal "prepare" methods ********
  var preparePromisesTable: Map[String,Promise[PrepareResponse]] = Map.empty

  def preparePayment(address: String, payment: Payment, instructions: Option[Instructions] = None): Future[PrepareResponse] = {
    val paymentParam: PaymentParam = PaymentParam(address, payment, instructions)
    execute("preparePayment", paymentParam).map(decode[PrepareResponse](_).right.get)
  }

  case class PaymentParam(address: String, payment: Payment, instructions: Option[Instructions]) extends RippleAPIObject

  case class UniversalPrepareResponse(success: Boolean, response: PrepareResponse) extends RippleAPIObject

  case class PrepareResponse(txJSON: String, instructions: Instructions) extends RippleAPIObject

  //----------------

  // *************** general tools **************

  private var callCounter=0
  private def _callId = {
    callCounter+=1
    callCounter
  }

  val SpecificNullValue = "This is null".asJson
  def execute(methodName: String, parameters: RippleAPIObject) = {
    val callId = _callId
    val p = Promise[String]()

    promisesTable += (callId->p)
    implicit val encodeNullableString: Encoder[Nullable[String]] = new Encoder[Nullable[String]] {
      final def apply(a: Nullable[String]): Json = {
        a.value match {
          case None => SpecificNullValue
          case Some(v) => v.asJson
        }
      }
    }
    implicit val encodeNullableString: Encoder[Nullable[Int]] = new Encoder[Nullable[Int]] {
      final def apply(a: Nullable[Int]): Json = {
        a.value match {
          case None => SpecificNullValue
          case Some(v) => v.asJson
        }
      }
    }
    implicit val encodeAPIOption = deriveEncoder[APIOption]
    println(parameters)
    def cleanJsonObject(obj: JsonObject) = {
      JsonObject.fromIterable(
        obj.toMap.head._2.asObject.get.toList.filter({
          case (_,value) => !value.isNull
          case _ => true
        }).map({
          case (k,value) => if (value == SpecificNullValue){
            (k,Json.Null)
          } else {
            (k,value)
          }
        })
      )
    }
    val options = js.Dynamic.literal(
      callId = callId,
      methodName = methodName,
      parameters = cleanJsonObject(parameters.asJsonObject).asJson.noSpaces
    )
    js.Dynamic.global.console.log(options)
    val target = dom.document.getElementById("ripple-api-sandbox").asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(options,"*")
    p.future
  }

  def onMessage(msg: dom.MessageEvent): Unit = {
    val callId: Int = msg.data.asInstanceOf[JSONObject].getInt("success")
    implicit val decodeNullableString: Decoder[Nullable[String]] = new Decoder[Nullable[String]] {
      final def apply(c: HCursor): Decoder.Result[Nullable[String]] = {
        c.value match {
          case Json.Null => Right(Nullable[String](None))
          case _ => Right(Nullable[String](Some(c.value.asInstanceOf[String])))

        }
      }
    }
    implicit val decodeNullableInt: Decoder[Nullable[Int]] = new Decoder[Nullable[Int]] {
      final def apply(c: HCursor): Decoder.Result[Nullable[Int]] = {
        c.value match {
          case Json.Null => Right(Nullable[Int](None))
          case _ => Right(Nullable[Int](Some(c.value.asInstanceOf[Int])))
        }
      }
    }
  }
  dom.document.addEventListener("message", { (e: dom.MessageEvent) => this.onMessage(e)}) //can't figure out how to pass onMessage to the event listener
}