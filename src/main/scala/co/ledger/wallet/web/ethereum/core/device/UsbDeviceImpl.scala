package co.ledger.wallet.web.ethereum.core.device

import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.DeviceManager.{ConnectivityType, ConnectivityTypes}
import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.web.ethereum.core.device.UsbDeviceFactory.HidDeviceInfo
import co.ledger.wallet.web.ethereum.core.device.UsbDeviceImpl.Connection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  *
  * UsbDeviceImpl
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 26/05/2016.
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
class UsbDeviceImpl(deviceInfo: HidDeviceInfo) extends Device {
  private val chrome = js.Dynamic.global.chrome

  override def connect(): Future[Device] = {
    _connectionPromise.getOrElse({
      _connectionPromise = Option(Promise())
      chrome.hid.connect(deviceInfo.deviceId, {(connection: UsbDeviceImpl.Connection) =>
        _connectionPromise.get.success(connection)
      })
      _connectionPromise.get
    }).future.map({(_) => this})
  }

  override def connectivityType: ConnectivityType = ConnectivityTypes.Usb

  override def debug_=(enable: Boolean): Unit = ???

  override def disconnect(): Unit = ???

  override def isDebugEnabled: Boolean = ???

  override def readyForExchange: Future[Unit] = ???

  override def isExchanging: Boolean = ???

  override def name: String = ???

  override def isConnected: Boolean = _connectionPromise.exists(_.isCompleted)

  override def eventBus: EventEmitter = _emitter

  override def info: String = ""

  override def toString: String =
    s"""Device Id: ${deviceInfo.deviceId}
        |Product Id: ${deviceInfo.productId}
        |Vendor Id: ${deviceInfo.vendorId}
        |Product Name: ${deviceInfo.productName}
    """.stripMargin

  @throws[AssertionError]("If there is already an exchange going on")
  override def exchange(command: Array[Byte]): Future[Array[Byte]] = ???

  override def matchInfo(info: String): Future[Boolean] = Future.successful(false)

  override def isConnecting: Boolean = _connectionPromise.exists(!_.isCompleted)

  private var _connectionPromise: Option[Promise[Connection]] = None
  private val _emitter: EventEmitter = null
}

object UsbDeviceImpl {

  @js.native
  trait Connection extends js.Object {
    val connectionId: Int = js.native
  }

}