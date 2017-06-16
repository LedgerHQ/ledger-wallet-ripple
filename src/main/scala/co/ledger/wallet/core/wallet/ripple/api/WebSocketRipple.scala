package co.ledger.wallet.core.wallet.ripple.api

import co.ledger.wallet.core.device.utils.{EventEmitter, EventReceiver}
import co.ledger.wallet.core.net.{WebSocket, WebSocketFactory}
import co.ledger.wallet.core.wallet.ripple.XRP
import co.ledger.wallet.core.wallet.ripple.events.{NewBlock, NewTransaction}
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.core.utils.ChromeGlobalPreferences
import co.ledger.wallet.web.ripple.wallet.RippleLibApi.LedgerEvent
import co.ledger.wallet.web.ripple.wallet.RippleWalletClient
import exceptions.{DisconnectedException, MissingTagException}
import org.json.JSONObject

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js.timers
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.timers.{SetTimeoutHandle, clearTimeout, setTimeout}

/**
  * Created by alix on 5/2/17.
  */
class WebSocketRipple(factory: WebSocketFactory,
                      addresses: Array[String],
                     wallet: RippleWalletClient) {
  import WebsocketRipple._

  def start(): Unit = {
    if (_socket.isEmpty) {
      connect()
    }
  }
  var connected = false

  private var _ws: Option[WebSocket] = None
  private def connect(): Unit = {
    println("connectiong socket")
    _socket = Some(factory.connect(""))
    _socket.get onComplete {
      case Success(ws) =>
        println("success socket")
        _ws = Some(ws)
        connected = true
        val subscribeMessage = js.Dynamic.literal(
          command = "subscribe",
          accounts = js.Array(addresses(0))) //TODO: change in case of multi account
        ws.send(js.JSON.stringify(subscribeMessage))
        ws.onJsonMessage(onMessage _)
        ws onClose { (ex) =>
          println("close websocket")
          connected = false
          emmiter.emit(WebsocketDisconnectedEvent())
          ex.printStackTrace()
          if (isRunning)
            connect()
        }
      case Failure(ex) =>
        println("failure websocket")
        if (isRunning)
          connect()
    }
  }

  private def onMessage(json: JSONObject): Unit = {
    println("websocket triggered", json)
    if (json.optString("type", "") == "transaction" && json.optBoolean("validated", false) && json.optString("engine_result", "") == "tesSUCCESS") {
      setTimeout(2000) {
        wallet.synchronize()
      }
    }

    if (json.optString("type","") == "transaction" &&
      json.optBoolean("validated", false) &&
      json.optJSONObject("transaction").optString("Account","") == addresses(0) &&
      json.optJSONObject("meta").optString("TransactionResult","") == "tesSUCCESS"){
      emmiter.emit(WebsocketTransactionSentEvent(json.optJSONObject("transaction").optString("TxnSignature","")))
    }

    if (json.optString("type","") == "transaction" &&
      json.optJSONObject("transaction").optString("Account","") == addresses(0) &&
      json.optJSONObject("meta").optString("TransactionResult","") == "tecDST_TAG_NEEDED"){
      emmiter.emit(WebsocketErrorEvent("tecDST_TAG_NEEDED", json.optJSONObject("transaction").optString("TxnSignature","")))
    }

    if (json.optString("type","") == "response" &&
      json.optJSONObject("result").optString("Account","") == addresses(0) &&
      json.optJSONObject("result").optString("Balance","") != "") {
      emmiter.emit(WebsocketResponseEvent("balance", json))
    }

    if (json.optString("type","") == "response" &&
      json.optJSONObject("result").optString("account","") == addresses(0) &&
      json.optJSONObject("result").has("transactions")) {
      emmiter.emit(WebsocketResponseEvent("transactions", json))
    }

  }

  def stop(): Unit = {
    _socket foreach {(s) =>
      _socket = None
      s.foreach(_.close())
    }
  }

  def balance(): Future[XRP] = {
    val balance = js.Dynamic.literal(
    command = "account_info",
    account = js.Array(addresses(0)))
    _ws.get.send(js.JSON.stringify(balance))
    val promise = Promise[XRP]()
    var timeOut: SetTimeoutHandle = null
    val receiver: EventReceiver = new EventReceiver {
      override def receive = {
        case WebsocketResponseEvent(name, bal) =>
          if (name == "balance") {
            clearTimeout(timeOut)
            WebSocketRipple.this.emmiter.unregister(this)
            promise.success(XRP(bal.optJSONObject("result").optString("Balance","")))
          }
        case WebsocketDisconnectedEvent() =>
          clearTimeout(timeOut)
          WebSocketRipple.this.emmiter.unregister(this)
          promise.failure(DisconnectedException())
        case WebsocketErrorEvent(name, data) =>
          clearTimeout(timeOut)
          WebSocketRipple.this.emmiter.unregister(this)
          promise.failure(DisconnectedException())

        case all =>
      }
      timeOut = setTimeout(80000){
        WebSocketRipple.this.emmiter.unregister(this)
        promise.failure(new Exception("Network timed out"))
      }
    }
    this.emmiter.register(receiver)
    promise.future
  }

  def transactions(ledger_min: Long = -1): Future[Array[JsonTransaction]] = {
    var offset: Int = 0
    var transactionsBuffer = ArrayBuffer[JsonTransaction]()
    def iterate(off: Int = 0):Future[Array[JsonTransaction]] = {
      val txs = js.Dynamic.literal(
        command = "account_tx",
        account = js.Array(addresses(0)),
        ledger_index_min = ledger_min,
        forward = true,
        offset = off
      )
      _ws.get.send(js.JSON.stringify(txs))
      val promise = Promise[Array[JsonTransaction]]()
      var timeOut: SetTimeoutHandle = null
      val receiver: EventReceiver = new EventReceiver {
        override def receive = {
          case WebsocketResponseEvent(name,json) =>
            if (name == "transactions") {
              clearTimeout(timeOut)
              WebSocketRipple.this.emmiter.unregister(this)
              if (json.getJSONArray("transactions").length() > 0) {
                val txs = json.getJSONArray("transactions")
                (0 until txs.length()) map { (index: Int) =>
                  transactionsBuffer.append(new JsonTransaction(txs.getJSONObject(index)))
                }
                offset = offset + json.getJSONArray("transactions").length()
                iterate(offset)
              } else {
                promise.success(transactionsBuffer.toArray)
              }
            }
          case WebsocketDisconnectedEvent() =>
            clearTimeout(timeOut)
            WebSocketRipple.this.emmiter.unregister(this)
            promise.failure(DisconnectedException())
          case WebsocketErrorEvent(name, data) =>
            clearTimeout(timeOut)
            WebSocketRipple.this.emmiter.unregister(this)
            promise.failure(DisconnectedException())

          case all =>
        }
        timeOut = setTimeout(80000){
          WebSocketRipple.this.emmiter.unregister(this)
          promise.failure(new Exception("Network timed out"))
        }
      }
      this.emmiter.register(receiver)
      promise.future
    }
    iterate()
  }

  val emmiter = new JsEventEmitter

  def isRunning = _socket.isDefined

  private var _socket: Option[Future[WebSocket]] = None
}

object WebsocketRipple {
  case class WebsocketTransactionSentEvent(txn: String)
  case class WebsocketDisconnectedEvent()
  case class WebsocketErrorEvent(name: String, data: String)
  case class WebsocketTransactionReceivedEvent(txn: String, tx: JSONObject)
  case class WebsocketResponseEvent(name: String, response: JSONObject)

}