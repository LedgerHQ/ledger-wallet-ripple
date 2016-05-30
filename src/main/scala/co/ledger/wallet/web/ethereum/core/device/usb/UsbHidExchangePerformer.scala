package co.ledger.wallet.web.ethereum.core.device.usb

import co.ledger.wallet.core.device.Device.CommunicationException
import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.web.ethereum.core.device.LedgerTransportHelper
import co.ledger.wallet.web.ethereum.core.device.usb.UsbDeviceImpl.UsbExchangePerformer

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  *
  * UsbHidDeviceImpl
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 27/05/2016.
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
class UsbHidExchangePerformer(connection: UsbDeviceImpl.Connection,
                              var debug: Boolean,
                              var useLedgerTransport: Boolean
                             ) extends UsbExchangePerformer {
  val HidBufferSize = 64
  val LedgerDefaultChannel = 1
  val Sw1DataAvailable = 0x61

  private val chrome = js.Dynamic.global.chrome

  override def close(): Unit = {
    chrome.hid.disconnect(connection.connectionId)
  }

  override def performExchange(cmd: Array[Byte]): Future[Array[Byte]] = {
    var command = cmd
    println("=> " + HexUtils.encodeHex(command))
    if (useLedgerTransport) {
      command = LedgerTransportHelper.wrapCommandAPDU(LedgerDefaultChannel, cmd, HidBufferSize)
    }
    def sendBlocks(offset: Int = 0): Future[Unit] = {
      val blockSize = if (command.length - offset > HidBufferSize) HidBufferSize else command.length - offset
      System.arraycopy(command, offset, _transferBuffer, 0, blockSize)
      send(_transferBuffer) flatMap {(_) =>
        if (offset + blockSize < command.length)
          sendBlocks(offset + blockSize)
        else
          Future.successful()
      }
    }
    def receiveLegacyBlock(buffer: ArrayBuffer[Byte]): Future[Array[Byte]] = {
      null
    }
    def receiveLedgerBlock(buffer: ArrayBuffer[Byte]): Future[Array[Byte]] = {
      receive().flatMap {(response) =>
        buffer ++= response
        val responseData = LedgerTransportHelper.unwrapResponseAPDU(LedgerDefaultChannel, buffer.toArray, HidBufferSize)
        if (responseData == null) {
          receiveLedgerBlock(buffer)
        } else {
          Future.successful(responseData)
        }
      }
    }
    def receiveBlocks(buffer: ArrayBuffer[Byte] = ArrayBuffer.empty[Byte]): Future[Array[Byte]] = {
      if (useLedgerTransport)
        receiveLedgerBlock(buffer)
      else
        receiveLegacyBlock(buffer)
    }
    sendBlocks().flatMap((_) => receiveBlocks()) andThen {
      case Success(result) =>
        println("<= " + HexUtils.encodeHex(result))
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  private def send(bytes: Array[Byte]): Future[Unit] = {
    import scala.scalajs.js.typedarray._
    val promise = Promise[Unit]()
    chrome.hid.send(connection.connectionId, 0, byteArray2Int8Array(bytes).buffer, { () =>
      if (js.isUndefined(chrome.runtime.lastError))
        promise.success()
      else
        promise.failure(CommunicationException(chrome.runtime.lastError.message.toString))
    })
    promise.future
  }

  private def receive(): Future[Array[Byte]] = {
    import scala.scalajs.js.typedarray._
    val promise = Promise[Array[Byte]]()
    chrome.hid.receive(connection.connectionId, {(reportId: Int, data: TypedArray[_, _]) =>
      if (js.isUndefined(chrome.runtime.lastError))
        promise.success(int8Array2ByteArray(new Int8Array(data)))
      else
        promise.failure(CommunicationException(chrome.runtime.lastError.message.toString))
    })
    promise.future
  }

  private val _transferBuffer = new Array[Byte](HidBufferSize)
}
