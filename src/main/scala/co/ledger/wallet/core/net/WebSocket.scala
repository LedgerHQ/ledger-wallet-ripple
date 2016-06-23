package co.ledger.wallet.core.net

import org.json.{JSONArray, JSONObject}

/**
  *
  * WebSocket
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
trait WebSocket {

  def send(data: String): Unit
  def send(data: Array[Byte]): Unit
  def send(json: JSONObject): Unit = send(json.toString)
  def send(json: JSONArray): Unit = send(json.toString)
  def close(): Unit
  def isOpen: Boolean
  def isClosed = !isOpen
  def onOpen(handler: () => Unit): Unit = openHandler = Option(handler)
  def onJsonMessage(handler: JSONObject => Unit): Unit = jsonHandler = Option(handler)
  def onStringMessage(handler: String => Unit): Unit = stringHandler = Option(handler)
  def onClose(handler: (Throwable) => Unit): Unit = closeHandler = Option(handler)
  def onError(handler: Throwable => Unit): Unit = errorHandler = Option(handler)

  protected var openHandler: Option[() => Unit] = None
  protected var jsonHandler: Option[(JSONObject) => Unit] = None
  protected var stringHandler: Option[(String) => Unit] = None
  protected var closeHandler: Option[(Throwable) => Unit] = None
  protected var errorHandler: Option[(Throwable) => Unit] = None

}
