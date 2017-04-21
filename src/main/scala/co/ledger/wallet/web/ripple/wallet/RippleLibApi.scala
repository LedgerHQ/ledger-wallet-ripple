package co.ledger.wallet.web.ripple.wallet

import java.time.LocalTime
import java.util.{Currency, Observable}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import concurrent.Future
import concurrent.Promise
import io.circe.parser._
import io.circe.Printer
import io.circe.syntax._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try
import io.circe.syntax._
import io.circe._
import io.circe.generic.semiauto._
import co.ledger.wallet.core.utils.Nullable
import io.circe.generic.auto._
import io.circe.generic.JsonCodec
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLIFrameElement


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

sealed trait RippleAPIObject {
}


class RippleLibApi() {

  implicit val encodeNullableString: Encoder[Nullable[String]] =
    new Encoder[Nullable[String]] {
      final def apply(a: Nullable[String]): Json = {
        a.value match {
          case None => SpecificNullValue
          case Some(v) => v.asJson
        }
      }
    }
  implicit val encodeNullableInt: Encoder[Nullable[Int]] =
    new Encoder[Nullable[Int]] {
      final def apply(a: Nullable[Int]): Json = {
        a.value match {
          case None => SpecificNullValue
          case Some(v) => v.asJson
        }
      }
    }


  @JsonCodec case class APIOption(
                                   server: Option[String] = Some("wss://s1.ripple.com"),
                                   feeCushion: Option[Double] = None,
                                   trace: Option[Boolean] = None,
                                   proxy: Option[Nullable[String]] = None,// = Some(Nullable[String](None)),
                                   timeout: Option[Long] = None
                                 ) extends RippleAPIObject
  type RippleSequence = Int
  type RippleDateTime = String
  var promisesTable: Map[Int,Promise[String]] = Map.empty
  /*def disconnect(): Future[SetOptionsResponse]  = {
    val methodName = "disconnect"
    execute(methodName, APIOption()).map(decode[SetOptionsResponse](_)
        .right.get)
  }*/

  //*************** setOptions *******************
  var setOptionsPromisesTable: Map[String,Promise[SetOptionsResponse]] =
    Map.empty



  def setOptions(options: APIOption): Future[SetOptionsResponse] ={
    val methodName = "setOption"
    execute(methodName, options).map(decode[SetOptionsResponse](_).right.get)
  }

  case class SetOptionsResponse(connected: Boolean,
                                info: String)

  //-----------------------------------------------------
  //****************** make call
  /*def preparePayment(parameters: PaymentParam): Future[PrepareResponse] = {
    execute("preparePayment", parameters)
      .map(decode[PrepareResponse](_).right.get)
  }

  def sign(parameters: SignParam): Future[SignedTransaction] = {
    execute("sign", parameters)
      .map(decode[SignedTransaction](_).right.get)
  }

  def submit(parameters: SubmitParam): Future[SubmittedTransaction] = {
    execute("submit", parameters)
      .map(decode[SubmittedTransaction](_).right.get)
  }*/
  //--------------------------------------

  //****************** call classes **********
  @JsonCodec case class Instructions(
                                      fee: Int,
                                      maxLedgerVersion: Nullable[Int] = Nullable[Int](None),
                                      maxLedgerVersionOffset: Option[Int] = None,
                                      sequence: Option[Long] = None
                                    )

  @JsonCodec case class Source(
                                address: String,
                                amount: Option[LaxAmount] = None,
                                tag: Option[Int] = None,
                                maxAmount: Option[LaxAmount] = None
                              )

  @JsonCodec case class LaxAmount(
                                   currency: String = "XRP",
                                   counterparty: Option[String] = None,
                                   value: Option[String] = None
                                 )

  @JsonCodec case class Destination(
                                     address: String,
                                     amount: Option[LaxAmount] = None,
                                     tag: Option[Int] = None,
                                     minAmount: Option[LaxAmount] = None
                                   )

  @JsonCodec case class Payment(
                                 source: Source,
                                 destination: Destination,
                                 allowPartialPayment: Option[Boolean] = None,
                                 invoiceID: Option[String] = None,
                                 limitQuality: Option[Boolean] = None,
                                 memos: Option[Array[Memo]] = None,
                                 noDirectRipple: Option[Boolean] = None,
                                 paths: Option[String] = None
                               )

  @JsonCodec case class Memo(
                              data: Option[String] = None,
                              format: Option[String] = None,
                              `type`: Option[String] = None
                            )

  /*case class Trustline(
                      currency: String, //currency
                      counterparty: String, //address
                      limit:Int, //value
                      authorized: Option[Boolean],
                      frozen: Option[Boolean],
                      memos: Option[Array[Memo]],
                      qualityIn: Option[Double],
                      qualityOut: Option[Double],
                      ripplingDisabled: Option[Boolean]
                      )*/

  @JsonCodec case class Options(
                                 binary: Option[Boolean] = None,
                                 counterparty: Option[String] = None,
                                 earliestFirst: Option[Boolean] = None,
                                 excludeFailures: Option[Boolean] = None,
                                 initiated: Option[Boolean] = None,
                                 limit: Option[Int] = None,
                                 maxLedgerVersion: Option[Int] = None,
                                 minLedgerVersion: Option[Int] = None,
                                 start: Option[String] = None,
                                 types: Option[String] = None,
                                 currency: Option[String] = None,
                                 ledgerVersion: Option[Int] = None,
                                 excludeAddresses: Option[String] = None,
                                 includeAllData: Option[Boolean] = None,
                                 includeState: Option[Boolean] = None,
                                 includeTransactions: Option[Boolean] = None,
                                 signAs: Option[String] = None,
                                 algorithm: Option[String] = None,
                                 entropy: Option[Array[Int]] = None
                               )

  //--------------------
  //************** Universal "prepare" methods ********

  @JsonCodec case class PaymentParam(
                                      address: String,
                                      payment: Payment,
                                      instructions: Instructions
                                    ) extends RippleAPIObject

  @JsonCodec case class PrepareResponse(
                                         txJSON: String,
                                         instructions: Instructions
                                       )


  //----------------

  //*************** signing tools ******************
  @JsonCodec case class SignParam(
                                   txJSON: String,
                                   secret: String,
                                   options: Option[Options] = None
                                 ) extends RippleAPIObject

  @JsonCodec case class SignedTransaction(
                                           signedTransaction: String,
                                           id: String
                                         )
  //---------------

  //*************** submitting tools ******************

  @JsonCodec case class SubmitParam(
                                     signedTransaction: String
                                   ) extends RippleAPIObject

  @JsonCodec case class SubmittedTransaction(
                                              resultCode: String,
                                              resultMessage: String
                                            )
  //---------------

  //*************** sync tools

  def getTransaction(parameters: GetTransactionParam) = {
    execute("getTransaction", parameters)
      .map(decode[Transaction](_).right.get)
  }



  def getTransactions(parameters: GetTransactionsParam) = {
    println("getTransactions called")

    execute("getTransactions", parameters)
      .map(decode[Array[Transaction]](_).right.get)
  }

  @JsonCodec case class GetTransactionParam(
                                             id: String,
                                             options: Option[TransactionOptions] = None
                                           ) extends RippleAPIObject

  @JsonCodec case class GetTransactionsParam(
                                              address: String,
                                              options: Option[TransactionsOptions] = None
                                            ) extends RippleAPIObject

  @JsonCodec case class TransactionsOptions(
                                             binary: Option[Boolean] = None,
                                             counterparty: Option[String] = None,
                                             earliestFirst: Option[Boolean] = None,
                                             excludeFailures: Option[Boolean] = None,
                                             initiated: Option[Boolean] = None,
                                             limit: Option[Int] = None,
                                             maxLedgerVersion: Option[Int] = None,
                                             minLedgerVersion: Option[Int] = None,
                                             start: Option[String] = None,
                                             types: Option[Array[String]] = None
                                           )

  @JsonCodec case class TransactionOptions(
                                            maxLedgerVersion: Option[Int] = None,
                                            minLedgerVersion: Option[Int] = None
                                          )

  @JsonCodec case class Transaction(
                                     id: String,
                                     address: String,
                                     sequence: Int,
                                     `type`: String,
                                     specification: Payment,
                                     outcome: Outcome
                                   )

  @JsonCodec case class Outcome(
                                 result: String,
                                 fee: String,
                                 balanceChanges: Map[String,Array[LaxAmount]],
                                 orderbookChanges: Map[String, OrderbookChanges],
                                 //implement balance changing keys
                                 ledgerVersion: Int,
                                 indexInLedger: Int,
                                 deliveredAmount: Option[String] = None,
                                 timestamp: Option[String] = None
                               )

  @JsonCodec case class OrderbookChanges(
                                          direction: String,
                                          quantity: LaxAmount,
                                          totalPrice: LaxAmount,
                                          sequence: RippleSequence,
                                          status: String,
                                          expirationTime: Option[RippleDateTime] = None,
                                          markerExchangeRate: Option[String] = None
                                        )


  //------------------


  // *************** general tools **************

  private var callCounter=0
  private def _callId = {
    callCounter+=1
    callCounter
  }


  implicit val decodeNullableString: Decoder[Nullable[String]] =
    new Decoder[Nullable[String]] {
      final def apply(c: HCursor): Decoder.Result[Nullable[String]] = {
        c.value match {
          case Json.Null => Right(Nullable[String](None))
          case _ => Right(Nullable[String](Some(c.value.asInstanceOf[String])))

        }
      }
    }
  implicit val decodeNullableInt: Decoder[Nullable[Int]] =
    new Decoder[Nullable[Int]] {
      final def apply(c: HCursor): Decoder.Result[Nullable[Int]] = {
        c.value match {
          case Json.Null => Right(Nullable[Int](None))
          case _ => Right(Nullable[Int](Some(c.value.asInstanceOf[Int])))
        }
      }
    }

  implicit  val decode1 = deriveDecoder[SetOptionsResponse]

  val SpecificNullValue = "This is null".asJson

  /*implicit val encodeAPIOption = deriveEncoder[APIOption]
  implicit val encode2 = deriveEncoder[SubmitParam]
  implicit val encode5 = deriveEncoder[LaxAmount]
  implicit val encode6 = deriveEncoder[SubmittedTransaction]
  implicit val encode7 = deriveEncoder[Source]
  implicit val encode10 = deriveEncoder[TransactionsOptions]
  implicit val encode11 = deriveEncoder[TransactionOptions]
  implicit val encode13 = deriveEncoder[OrderbookChanges]
  implicit val encode16 = deriveEncoder[Instructions]
  implicit val encode17 = deriveEncoder[Destination]
  implicit val encode19 = deriveEncoder[Memo]
  implicit val encode20 = deriveEncoder[Options]
  implicit val encode21 = deriveEncoder[SignedTransaction]
  implicit val encode4 = deriveEncoder[GetTransactionsParam]
  implicit val encode18 = deriveEncoder[Payment]
  implicit val encode14 = deriveEncoder[PrepareResponse]
  implicit val encode15 = deriveEncoder[PaymentParam]
  implicit val encode12 = deriveEncoder[Outcome]
  implicit val encode8 = deriveEncoder[SignParam]
  implicit val encode9 = deriveEncoder[Transaction]
  implicit val encode3 = deriveEncoder[GetTransactionParam]
  implicit val encode1 = deriveEncoder[SignParam]*/

  implicit val encodeRippleAPIObject: ObjectEncoder[RippleAPIObject] = {
    new ObjectEncoder[RippleAPIObject] {
      override def encodeObject(obj: RippleAPIObject): JsonObject = {
        obj match {
          case o: APIOption => o.asJsonObject
          case o: GetTransactionParam => o.asJsonObject
          case o: GetTransactionsParam => o.asJsonObject
          case o: PaymentParam => o.asJsonObject
          case o: SignParam => o.asJsonObject
          case o: SubmitParam => o.asJsonObject
        }
      }
    }
  }

  def execute(methodName: String, parameters: RippleAPIObject) = {

    val callId = _callId
    val p = Promise[String]()

    promisesTable += (callId->p)



    def cleanJsonObject(obj: JsonObject): JsonObject = {
      JsonObject.fromIterable(
        obj.toMap.toList.filter({
          case (_,value) => !value.isNull
        }).map({
          case (k,value) => if (value == SpecificNullValue){
            (k,Json.Null)
          } else if (value.isObject) {
            (k,Json.fromJsonObject(cleanJsonObject(value.asObject.get)))
          } else if (value.isArray) {
            (k,Json.fromValues(cleanJsonArray(value.asArray.get)))
          } else {
            (k,value)
          }
        })
      )
    }

    def cleanJsonArray(obj: Vector[Json]): Vector[Json] = {
      obj filter {(value) =>
        !value.isNull
      } map {(value) =>
        if (value == SpecificNullValue) {
          Json.Null
        } else if (value.isObject) {
          Json.fromJsonObject(cleanJsonObject(value.asObject.get))
        } else if (value.isArray) {
          Json.fromValues(cleanJsonArray(value.asArray.get))
        } else {
          value
        }
      }
    }

    val options = js.Dynamic.literal(
      call_id = callId,
      method_name = methodName,
      parameters = cleanJsonObject(parameters.asJsonObject).asJson.noSpaces
    )

    js.Dynamic.global.console.log(options)
    val target = dom.document.getElementById("ripple-lib-api-sandbox")
      .asInstanceOf[HTMLIFrameElement]
    target.contentWindow.postMessage(options,"*")
    p.future
  }


  def onMessage(msg: dom.MessageEvent): Unit = {
    println("onMessage called")
    val callId: Int = msg.data.asInstanceOf[js.Dynamic].call_id
      .asInstanceOf[Int]
    val p = promisesTable.get(callId).get
    js.Dynamic.global.console.log(msg.data.asInstanceOf[js.Dynamic].response)
    p success msg.data.asInstanceOf[js.Dynamic].response.asInstanceOf[String]
    promisesTable -= callId
  }


  case class PostMessage(callId: Int, response: String)

  dom.window.addEventListener("message", { (e: dom.MessageEvent) =>
    onMessage(e)
  })


  //------------------

  //****************** error event management
  dom.window.addEventListener("error", { (e: dom.ErrorEvent) => onError(e)})

  case class Error(
                    errorCode: String,
                    errorMessage: String,
                    data: String
                  )

  def onError(e: dom.ErrorEvent) = {
    println("error received in scala")
    println(e)
  }


  //------------------

  //****************** ledger event management
  dom.window.addEventListener("newLedger", { (e: dom.CustomEvent) => onLedger(e)})

  case class Ledger(
                     baseFeeXRP: Int, //value
                     ledgerHash: String,
                     ledgerTimestamp: String, // date-time-string
                     reserveBaseXRP: Int, //value
                     reserveIncrementXRp: Int, //value
                     transactionCount: Int,
                     ledgerVersion: Int,
                     validatedLedgerVersions: String
                   )

  def onLedger(e: dom.CustomEvent) = {
    //println("new ledger received")
  }


  //------------------
}