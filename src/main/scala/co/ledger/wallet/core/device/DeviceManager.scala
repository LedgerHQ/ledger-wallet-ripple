/**
  *
  * DeviceManager
  * Ledger wallet
  *
  * Created by Pierre Pollastri on 15/01/16.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 Ledger
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
package co.ledger.wallet.core.device

import java.util.UUID

import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.core.utils.Preferences

import scala.concurrent.{ExecutionContext, Future, Promise}

trait DeviceManager[Context]  {
  import DeviceManager._

  implicit val ec: ExecutionContext

  def compatibleConnectivityTypes: Future[Set[ConnectivityType]] = Future {
    _deviceManager filter {
      case (t, m) => m.isCompatible
    } map {
      case (t, m) => t
    } toSet
  }

  def allCompatibleFactories: Iterable[DeviceFactory] = {
    _deviceManager filter {
      case (t, m) => m.isCompatible
    } map {
      case (t, m) => m
    }
  }

  def deviceFactory(connectivityType: ConnectivityType): DeviceFactory = {
    _deviceManager(connectivityType)
  }

  def requestScan(): ScanRequest = {
    val requests = allCompatibleFactories.map({(factory) =>
      factory.requestScan()
    })
    new CompoundScanRequest(requests)
  }

  def registerDevice(device: Device): Future[UUID] = Future {
    val uuid = UUID.randomUUID()
    device.uuid = uuid
    _registeredDevices(uuid) = device
    preferences.edit()
      .putString("last_device_type", connectivityTypeToString(device.connectivityType))
      .putString("last_device_info", device.info)
      .putString("last_device_uuid", uuid.toString)
      .commit()
    uuid
  }

  def unregisterDevice(uuid: UUID): Unit = Future {
    _registeredDevices.remove(uuid)
  }

  def unregisterDevice(device: Device): Unit = Future {
    // TODO: Rewrite
    _registeredDevices.retain((uuid, d) => d != device)
  }

  def connectedDevice(uuid: UUID): Future[Device] = Future {
    _registeredDevices.getOrElse(uuid, throw new Exception("No such device"))
  }

  def lastConnectedDevice(): Future[Device] = ???
    if (!preferences.contains("last_device_uuid"))
      Future.failed(new Exception("No last device"))
    else
      connectedDevice(UUID.fromString(preferences.string("last_device_uuid").orNull))

  def lastConnectedDeviceInfo(): Future[(DeviceFactory, String)] = Future {
    val deviceType = stringToConnectivityType(preferences.string("last_device_type").orNull)
    val deviceInfo = preferences.string("last_device_info").orNull
    (_deviceManager(deviceType), deviceInfo)
  }

  protected def preferences: Preferences
  def context: Context

  protected[this] val _registeredDevices = scala.collection.mutable.Map[UUID, Device]()

  import DeviceManager.ConnectivityTypes._
  protected val _deviceManager: Map[ConnectivityType, DeviceFactory]

  type DelayedFunctionHandler = (Long, () => Unit) => Unit
  protected def delayedFunctionHandler: DelayedFunctionHandler

  private class CompoundScanRequest(requests: Iterable[ScanRequest]) extends ScanRequest {

    override def start(): Unit = {
      requests foreach {
        _.duration = duration
      }
      super.start()
    }

    override def onStart(): Unit = {
      for (request <- requests) {
        request.start()
      }
    }

    override protected def runDelayed(delay: Long)(f: => Unit): Unit = delayedFunctionHandler(delay, f _)

    override def onStop(): Unit = {
      for (request <- requests) {
        request.stop()
      }
    }

    for (request <- requests) {
      request.onScanUpdate({
        case DeviceDiscovered(device) => notifyDeviceDiscovered(device)
        case DeviceLost(device) => notifyDeviceLost(device)
      })
    }
  }

  private def connectivityTypeToString(t: ConnectivityType): String = {
    t match {
      case Usb => "usb"
      case Ble => "ble"
      case Tee => "tee"
      case Nfc => "nfc"
    }
  }

  private def stringToConnectivityType(t: String): ConnectivityType = {
    t match {
      case "usb" => Usb
      case "ble" => Ble
      case "tee" => Tee
      case "nfc" => Nfc
    }
  }

}

object DeviceManager {

  object ConnectivityTypes extends Enumeration {
    type ConnectivityType = Value
    val Usb, Ble, Tee, Nfc = Value
  }

  type ConnectivityType = ConnectivityTypes.ConnectivityType

  case class AndroidDeviceNotCompatibleException(msg: String) extends Exception(msg)
  case class MissingPermissionException(msg: String) extends Exception(msg)
  case class DisabledServiceException(msg: String) extends Exception(msg)
}

