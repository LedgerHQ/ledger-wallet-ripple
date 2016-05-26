package co.ledger.wallet.web.ethereum.core.device

import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.device.DeviceFactory.{ScanRequest, ScanUpdate}
import co.ledger.wallet.core.device.DeviceManager.{ConnectivityType, ConnectivityTypes}
import co.ledger.wallet.web.ethereum.core.device.UsbDeviceFactory.HidDeviceInfo

import scala.concurrent.{ExecutionContext, Future, duration}
import scala.scalajs.js
import scala.scalajs.js.timers

/**
  *
  * UsbDeviceFactory
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
class UsbDeviceFactory extends DeviceFactory {
  /** *
    * Check if the android device is compatible with the technology (may block the current thread)
    *
    * @return true if compatible false otherwise
    */
  override def isCompatible: Boolean = true

  override def requestScan(): ScanRequest = new ScanRequestImpl

  override def connectivityType: ConnectivityType = ConnectivityTypes.Usb

  override def requestPermission(): Future[Unit] = Future.successful()

  /** *
    * Check if service is enabled (may block the current thread)
    *
    * @return true if enabled false otherwise
    */
  override def isEnabled: Boolean = true

  /** *
    * Check if the manager has enough permissions to run (may block the current thread)
    *
    * @return true if the manager has all required permissions false otherwise
    */
  override def hasPermissions: Boolean = true

  private class ScanRequestImpl extends ScanRequest {
    val chrome = js.Dynamic.global.chrome

    override def onStart(): Unit = {
      _running = true
      def tick(): Unit = {
        chrome.hid.getDevices(js.Dictionary(), {(devices: js.Array[HidDeviceInfo]) =>
          val diffs = _previousResult.map(_.deviceId).diff(devices.map(_.deviceId)).concat(devices.map(_.deviceId).diff(_previousResult.map(_.deviceId)))
          for (id <- diffs) {
            if (!_previousResult.exists(_.deviceId == id)) {
              val device = new UsbDeviceImpl(devices.find(_.deviceId == id).get)
              _devices(id) = device
              notifyDeviceDiscovered(device)
            } else {
              _devices.remove(id) foreach {(device) =>
                notifyDeviceLost(device)
              }
            }
          }
          _previousResult = devices
          scheduleTick()
        })
      }
      def scheduleTick(): Unit = {
        runDelayed(500) {
          if (_running)
            tick()
        }
      }
      scheduleTick()
    }

    override def onStop(): Unit = {
      _running = false
    }

    override protected def runDelayed(delay: Long)(f: => Unit): Unit = {
      import timers._
      setTimeout(delay) {
        f
      }
    }

    private val _devices = scala.collection.mutable.Map[Int, Device]()
    private var _running = false
    private var _previousResult = js.Array[HidDeviceInfo]()
  }

}


object UsbDeviceFactory {
  @js.native
  trait HidDeviceInfo extends js.Object {
    val deviceId: Int = js.native
    val vendorId: Int = js.native
    val productId: Int = js.native
    val productName: String = js.native
    val serialNumber: String = js.native
  }
}