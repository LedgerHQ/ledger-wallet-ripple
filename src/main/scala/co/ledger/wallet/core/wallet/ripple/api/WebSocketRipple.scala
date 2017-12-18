package co.ledger.wallet.core.wallet.ripple.api

import co.ledger.wallet.core.device.utils.{EventEmitter, EventReceiver}
import co.ledger.wallet.core.net.{WebSocket, WebSocketFactory}
import co.ledger.wallet.core.wallet.ripple.XRP
import co.ledger.wallet.core.wallet.ripple.events.{NewBlock, NewTransaction}
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.core.utils.ChromeGlobalPreferences
import co.ledger.wallet.web.ripple.wallet.RippleLibApi.LedgerEvent
import co.ledger.wallet.web.ripple.wallet.RippleWalletClient
import exceptions.{DisconnectedException, MissingTagException, RippleException}
import io.circe.JsonObject
import org.json.{JSONArray, JSONObject}
import org.scalajs.dom

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
  import WebSocketRipple._

  def start(): Unit = {
    if (_socket.isEmpty) {
      connect()
    }
  }
  var connecting = Promise[Unit]()
  var connected = false
  val emmiter = new JsEventEmitter
  private var _ws: Option[WebSocket] = None

  private def connect(): Unit = {
    println("connecting socket")
    _socket = Some(factory.connect(""))
    _socket.get onComplete {
      case Success(ws) =>
        println("success socket")
        _ws = Some(ws)
        ws.onJsonMessage(onMessage _)
        val subscribeMessage = js.Dynamic.literal(
          command = "subscribe",
          accounts = js.Array(addresses(0))) //TODO: change in case of multi account
        send(subscribeMessage) map {(msg) =>
          if (!connected) {
            println("Subscribed")
            connected = true
            connecting.success()
          }
        }
        ws onClose { (ex) =>
          println("close websocket")
          connecting = Promise[Unit]()
          connected = false
          emmiter.emit(WebSocketDisconnectedEvent())
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

  var promisesTable: Map[Int,Promise[JSONObject]] = Map.empty

  def onMessage(msg: JSONObject): Unit = {
    println("received",msg.toString.substring(0,400))
    if (msg.has("id")) {
      val callId = msg.getInt("id")
      val p = promisesTable.get(callId).get
      promisesTable -= callId
      if (!p.isCompleted){
        p success msg
      }
    }
    if (msg.optString("type","") == "transaction" && msg.optBoolean("validated", false) && msg.optJSONObject("transaction") != null) {
      if (msg.optJSONObject("transaction").optString("Account", "") == addresses(0) &&
        msg.optJSONObject("meta").optString("TransactionResult", "") == "tesSUCCESS") {
        println("transactions received")
        emmiter.emit(WebSocketTransactionSentEvent(msg.optJSONObject("transaction").optString("TxnSignature", "")))
      }
    }
    if (msg.optString("type", "") == "transaction" && msg.optBoolean("validated", false) && msg.optString("engine_result", "") == "tesSUCCESS") {
      if (msg.optJSONObject("transaction").optString("Account", addresses(0)) != addresses(0)) {
        wallet.synchronize()
      }
    }
    if (msg.optString("type","") == "transaction" && msg.optBoolean("validated", false) && msg.optJSONObject("transaction") != null) {
      if (msg.optJSONObject("transaction").optString("Account", "") == addresses(0) &&
        msg.optJSONObject("meta").optString("TransactionResult", "") == "tecDST_TAG_NEEDED") {
        emmiter.emit(WebSocketErrorEvent("tecDST_TAG_NEEDED", msg.optJSONObject("transaction").optString("TxnSignature", "")))
      }
    }

  }

  /*private def onMessage(json: JSONObject): Unit = {
    println(json.toString.substring(0,200))

    if (json.optString("type", "") == "transaction" && json.optBoolean("validated", false) && json.optString("engine_result", "") == "tesSUCCESS") {
      setTimeout(2000) {
        wallet.synchronize()
      }
    }

    if (json.optString("type","") == "response" && json.optString("status", "") == "success" ) {
      if (json.optJSONObject("result").has("account_data")) {
        if (json.optJSONObject("result").getJSONObject("account_data").optString("Account","") == addresses(0)) {
          println("balance received")
          emmiter.emit(WebSocketResponseEvent("balance", json))
        }
      }
    }

    if (json.toString == "{\"result\":{},\"status\":\"success\",\"type\":\"response\"}") {
      if (!connected) {
        println("Subscribed")
        connected = true
        connecting.success()
      }
    }

    if (json.optString("type","") == "transaction" && json.optBoolean("validated", false) && json.optJSONObject("transaction") != null) {
      if (json.optJSONObject("transaction").optString("Account", "") == addresses(0) &&
        json.optJSONObject("meta").optString("TransactionResult", "") == "tesSUCCESS") {
        println("transactions received")
        emmiter.emit(WebSocketTransactionSentEvent(json.optJSONObject("transaction").optString("TxnSignature", "")))
      }
    }

    if (json.optString("type","") == "transaction" && json.optBoolean("validated", false) && json.optJSONObject("transaction") != null) {
      if (json.optJSONObject("transaction").optString("Account", "") == addresses(0) &&
        json.optJSONObject("meta").optString("TransactionResult", "") == "tecDST_TAG_NEEDED") {
        emmiter.emit(WebSocketErrorEvent("tecDST_TAG_NEEDED", json.optJSONObject("transaction").optString("TxnSignature", "")))
      }
    }

    if (json.optString("type","") == "response" && json.optString("status", "") == "success" ) {
      if (json.optJSONObject("result").optString("account","") == addresses(0) &&
        json.optJSONObject("result").has("transactions")) {
        println("transactions received")
        println(json.getJSONObject("result").getJSONArray("transactions").length())
        emmiter.emit(WebSocketResponseEvent("transactions", json))
      }
    }

  }*/

  private var callCounter=0
  private def _callId = {
    callCounter+=1
    callCounter
  }

  def send(json: js.Dynamic) = {
    val callId = _callId +10
    val p = Promise[JSONObject]()
    json.updateDynamic("id")(callId)
    promisesTable += (callId->p)
    println("Sending", js.JSON.stringify(json))
    _ws.get.send(js.JSON.stringify(json))
    setTimeout(2000) {
      if (!p.isCompleted) {
        p failure(RippleException())
      }
    }
    p.future
  }

  def stop(): Unit = {
    _socket foreach {(s) =>
      _socket = None
      s.foreach(_.close())
    }
  }

  def balance(account: String = ""): Future[XRP] = {
    if (!connected) {
      Future.failed(DisconnectedException())
    } else {
      var target = account
      if (account == ""){
        target = addresses(0)
      }
      val balance = js.Dynamic.literal(
        command = "account_info",
        account = target)
      println("target",target)
      send(balance) map {(msg) =>
        if (msg.optString("status","error")=="success"){
          XRP(msg.optJSONObject("result").getJSONObject("account_data").optString("Balance", ""))
        } else {
          XRP.Zero
        }
      }
    }
  }

  def fee(): Future[XRP] = {
    if (!connected) {
      Future.failed(DisconnectedException())
    } else {
      val fee = js.Dynamic.literal(
        command = "fee" )
      send(fee) map {(msg) =>
        if (msg.optString("status","error")=="success"){
          XRP(msg.optJSONObject("result").getJSONObject("drops").optString("base_fee", "10"))
        } else {
          XRP(10)
        }
      }
    }
  }

  def transactions(ledger_min: Long = 0): Future[Array[JsonTransaction]] = {
    if (!connected) {
      Future.failed(DisconnectedException())
    } else {
      var offset: Int = 0
      var transactionsBuffer = ArrayBuffer[JsonTransaction]()

      def iterate(off: Int = 0): Future[Array[JsonTransaction]] = {
        val txs = js.Dynamic.literal(
          command = "account_tx",
          account = addresses(0),
          ledger_index_min = ledger_min,
          forward = true,
          offset = off
        )
        send(txs) flatMap {(json) =>
          println("length received",json.getJSONObject("result").getJSONArray("transactions").length())
          if (json.getJSONObject("result").getJSONArray("transactions").length() > 0) {
            val txs = json.getJSONObject("result").getJSONArray("transactions")
            (0 until txs.length()) map { (index: Int) =>
              if (txs.getJSONObject(index).getJSONObject("meta").getString("TransactionResult") == "tesSUCCESS") {
                transactionsBuffer.append(new JsonTransaction(txs.getJSONObject(index)))
                println(transactionsBuffer.last)
              }
            }
            println("buffer length",transactionsBuffer.length)
            offset = offset + json.getJSONObject("result").getJSONArray("transactions").length()
            iterate(offset)
          } else {
            Future.successful(transactionsBuffer.toArray)
          }
        }
      }
      iterate()
    }
  }


  def isRunning = _socket.isDefined

  private var _socket: Option[Future[WebSocket]] = None
}

object WebSocketRipple {
  case class WebSocketDisconnectedEvent()
  case class WebSocketErrorEvent(name: String, data: String)
  case class WebSocketTransactionSentEvent(txn: String)
  case class WebSocketTransactionReceivedEvent(txn: String, tx: JSONObject)
  case class WebSocketResponseEvent(name: String, response: JSONObject)

}
