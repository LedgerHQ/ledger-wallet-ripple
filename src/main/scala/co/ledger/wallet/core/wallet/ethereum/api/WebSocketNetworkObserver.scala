package co.ledger.wallet.core.wallet.ethereum.api

import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.core.net.{WebSocket, WebSocketFactory}
import co.ledger.wallet.core.utils.logs.Logger
import co.ledger.wallet.core.wallet.ethereum.events._
import org.json.JSONObject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  *
  * WebSocketNetworkObserver
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 23/06/2016.
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
class WebSocketNetworkObserver(factory: WebSocketFactory,
                               emitter: EventEmitter,
                               transactionRestClient: AbstractTransactionRestClient,
                               executionContext: ExecutionContext) {
  implicit val ec = executionContext

  def start(): Unit = {
    if (_socket.isEmpty) {
      connect()
    }
  }

  private def connect(): Unit = {
    _socket  = Some(factory.connect("/ws"))
    _socket.get onComplete {
      case Success(ws) =>
        ws.onJsonMessage(onMessage _)
        ws onClose {(ex) =>
          ex.printStackTrace()
          if (isRunning)
            connect()
        }
      case Failure(ex) =>
        if (isRunning)
          connect()
    }
  }

  private def onMessage(json: JSONObject): Unit = {
    val message = json.getJSONObject("payload")
    message.getString("type") match {
      case "new-transaction" =>
        emitter.emit(NewTransaction(new transactionRestClient.JsonTransaction(message.getJSONObject("transaction"))))
      case "new-block" =>
        emitter.emit(NewBlock(new transactionRestClient.blockRestClient.JsonBlock(message.getJSONObject("block"))))
      case other => Logger.v(s"Receive unhandled notification type '$other'")
    }
  }

  def stop(): Unit = {
    _socket foreach {(s) =>
      _socket = None
      s.foreach(_.close())
    }
  }

  def isRunning = _socket.isDefined

  private var _socket: Option[Future[WebSocket]] = None
}