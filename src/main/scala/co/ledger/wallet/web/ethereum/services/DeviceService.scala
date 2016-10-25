package co.ledger.wallet.web.ethereum.services

import java.util.UUID

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.Service
import biz.enef.angulate.core.Location
import co.ledger.wallet.core.device.Device.{Connect, Disconnect}
import co.ledger.wallet.core.device.DeviceManager.ConnectivityTypes.ConnectivityType
import co.ledger.wallet.core.device.utils.EventReceiver
import co.ledger.wallet.core.device.{Device, DeviceFactory, DeviceManager}
import co.ledger.wallet.core.utils.Preferences
import co.ledger.wallet.web.ethereum.core.device.usb.UsbDeviceFactory
import co.ledger.wallet.web.ethereum.core.utils.{ChromeGlobalPreferences, ChromePreferences}

import scala.concurrent.{ExecutionContext, Future, duration}
import scala.scalajs.js
import scala.scalajs.js.timers._


/**
  *
  * DeviceService
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 25/05/2016.
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
class DeviceService($location: Location,  $route: js.Dynamic, sessionService: SessionService) extends Service with DeviceManager[Any] {
  import co.ledger.wallet.core.device.DeviceManager.ConnectivityTypes._

  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override protected def delayedFunctionHandler: DelayedFunctionHandler = (delay: Long, f: () => Unit) => {
    import duration._
    setTimeout(delay.milliseconds)(f())
  }

  override protected def preferences: Preferences = _preferences

  override def context: Any = this

  override def registerDevice(device: Device): Future[UUID] = {
    device.eventBus.register(_eventReceiver)
    super.registerDevice(device)
  }

  override def unregisterDevice(device: Device): Unit = {
    device.eventBus.unregister(_eventReceiver)
    super.unregisterDevice(device)
  }

  override protected val _deviceManager: Map[ConnectivityType, DeviceFactory] = Map(
    Usb -> new UsbDeviceFactory
  )

  private val _preferences = new ChromeGlobalPreferences("DeviceService")

  private val _eventReceiver = new EventReceiver {
    override def receive: Receive = {
      case Connect(_) =>
      case Disconnect(_) =>
        sessionService.stopCurrentSessions()
        $location.path("/onboarding/launch")
        $route.reload()
    }
  }

  DeviceService.setInstance(this)
}

object DeviceService {
  def instance = _instance
  private def setInstance(service: DeviceService) = _instance = service
  private var _instance: DeviceService = null

  def init(module: RichModule) = {
    module.serviceOf[DeviceService]("deviceService")
  }

}