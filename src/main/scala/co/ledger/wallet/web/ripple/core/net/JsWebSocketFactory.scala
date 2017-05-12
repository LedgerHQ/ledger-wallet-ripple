package co.ledger.wallet.web.ripple.core.net

import java.net.URI

import co.ledger.wallet.core.net.{WebSocket, WebSocketFactory}
import org.json.JSONObject
import org.scalajs.dom.{CloseEvent, ErrorEvent}
import org.scalajs.dom.raw.{Event, MessageEvent}
import scala.concurrent.{Future, Promise}

/**
  *
  * JsWebsocketFactory
  * ledger-wallet-ripple-chrome
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
class JsWebSocketFactory(override val baseUrl: URI) extends WebSocketFactory {

  override def connect(path: String): Future[WebSocket] = {
    val promise = Promise[WebSocket]()
    val ws = new org.scalajs.dom.WebSocket(baseUrl.toString + (if (path.startsWith("/")) "" else "/") + path)
    println("ws created")
    ws.onopen = {(event: Event) =>
      println("ws open")
      promise.success(new JsWebSocket(ws))
    }
    ws.onerror = {(event: ErrorEvent) =>
      println("ws error")
      try {
        promise.failure(new Exception(event.message))
        println("failure succeed")
      } catch {
        case all: Throwable => {
          all.printStackTrace()
          println("unloggble trigggered")
          promise.failure(new Exception("Unlogable exception"))
        }
      }
    }
    promise.future
  }

  private class JsWebSocket(socket: org.scalajs.dom.WebSocket) extends WebSocket {
    override def send(data: String): Unit = socket.send(data)

    override def send(data: Array[Byte]): Unit = ???

    override def isOpen: Boolean = _isOpen

    override def close(): Unit = socket.close()

    private var _isOpen = true

    socket.onmessage = {(event: MessageEvent) =>
      stringHandler.foreach(_(event.data.asInstanceOf[String]))
      jsonHandler.foreach(_(new JSONObject(event.data.asInstanceOf[String])))
    }

    socket.onclose = {(event: CloseEvent) =>
      _isOpen = false
      closeHandler.foreach(_(new Exception(event.reason)))
    }

    socket.onerror = {(event: ErrorEvent) =>
      _isOpen = false
      closeHandler.foreach(_(new Exception(event.message)))
    }

  }

}


object JsWebSocketFactory {

}