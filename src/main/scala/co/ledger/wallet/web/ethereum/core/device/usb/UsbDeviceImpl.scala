package co.ledger.wallet.web.ethereum.core.device.usb

import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.Device.{Connect, Disconnect}
import co.ledger.wallet.core.device.DeviceManager.{ConnectivityType, ConnectivityTypes}
import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.web.ethereum.core.device.usb.UsbDeviceFactory.HidDeviceInfo
import co.ledger.wallet.web.ethereum.core.device.usb.UsbDeviceImpl.UsbExchangePerformer
import co.ledger.wallet.web.ethereum.core.event.JsEventEmitter

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, Promise}
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
        _exchanger = Some(new UsbHidExchangePerformer(connection, _debug, true))
        _connectionPromise.get.success(connection)
        _emitter.emit(Connect(this))
        chrome.hid.onDeviceRemoved.addListener(_callback)
      })
      _connectionPromise.get
    }).future.map({(_) => this})
  }

  override def connectivityType: ConnectivityType = ConnectivityTypes.Usb

  override def debug_=(enable: Boolean): Unit = _debug = enable

  override def disconnect(): Unit = {
    _exchanger.foreach(_.close())
    _exchanger = None
    _connectionPromise.foreach {(p) =>
      p.future foreach {(connection) =>
        chrome.hid.onDeviceRemoved.removeListener(_callback)
      }
    }
    eventBus.emit(Disconnect(this))
  }

  override def isDebugEnabled: Boolean = _debug

  override def readyForExchange: Future[Unit] = _exchanger.map(_.readyForExchange).getOrElse(Future.failed(new Exception("Not connected yet")))

  override def isExchanging: Boolean = _exchanger.exists(_.isExchanging)

  override def name: String = ""

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
  override def exchange(command: Array[Byte]): Future[Array[Byte]] =
    _exchanger.map(_.exchange(command)).getOrElse(throw new Exception("Not connected"))

  override def matchInfo(info: String): Future[Boolean] = Future.successful(false)

  override def isConnecting: Boolean = _connectionPromise.exists(!_.isCompleted)

  private var _connectionPromise: Option[Promise[UsbDeviceImpl.Connection]] = None
  private var _exchanger: Option[UsbExchangePerformer] = None
  private val _emitter: EventEmitter = new JsEventEmitter
  private var _debug = false
  private val _callback: js.Function = {(deviceId: Int) =>
    if (deviceId == deviceInfo.deviceId) {
      disconnect()
    }
  }
}

object UsbDeviceImpl {

  @js.native
  trait Connection extends js.Object {
    val connectionId: Int = js.native
  }

  trait UsbExchangePerformer {

    def close(): Unit
    def onDisconnect(callback: () => Unit)(implicit ec: ExecutionContext) = {
      _disconnectCallback = Option(callback)
      _disconnectCallbackEC = Option(ec)
    }

    def performExchange(command: Array[Byte]): Future[Array[Byte]]

    def exchange(command: Array[Byte])(implicit ec: ExecutionContext): Future[Array[Byte]]
    = synchronized {
      assume(!isExchanging, "There is already an exchange going on.")
      _exchangeFuture = Some(performExchange(command)
       recover {
        case err: Throwable =>
          _exchangeFuture = None
          throw err
      } map {(result) =>
        _exchangeFuture = None
        result
      })
      _exchangeFuture.get
    }

    def readyForExchange(implicit ec: ExecutionContext): Future[Unit] = {
      if (isExchanging) {
        _exchangeFuture.get.map((_) => ())
      } else {
        Future.successful()
      }
    }

    def isExchanging: Boolean = _exchangeFuture.isDefined

    private[this] var _exchangeFuture: Option[Future[Array[Byte]]] = None

    protected def notifyDisconnect(): Unit = {
      _disconnectCallbackEC.foreach {(ec) =>
        ec.execute(new Runnable {
          override def run(): Unit = _disconnectCallback foreach {(callback) =>
            callback()
          }
        })
      }
    }

    protected[this] var _disconnectCallback: Option[() => Unit] = None
    protected[this] var _disconnectCallbackEC: Option[ExecutionContext] = None
  }



}