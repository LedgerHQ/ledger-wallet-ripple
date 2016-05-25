package co.ledger.wallet.web.ethereum.services

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.Service
import co.ledger.wallet.core.device.DeviceManager.ConnectivityTypes.ConnectivityType
import co.ledger.wallet.core.device.{DeviceFactory, DeviceManager}
import co.ledger.wallet.core.utils.Preferences
import co.ledger.wallet.web.ethereum.core.utils.ChromePreferences

import scala.concurrent.{ExecutionContext, duration}
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
class DeviceService extends Service with DeviceManager[Any] {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override protected def delayedFunctionHandler: DelayedFunctionHandler = (delay: Long, f: () => Unit) => {
    import duration._
    setTimeout(delay.milliseconds)(f())
  }

  override protected def preferences: Preferences = _preferences

  override def context: Any = this

  override protected val _deviceManager: Map[ConnectivityType, DeviceFactory] = Map(

  )

  private val _preferences = new ChromePreferences()
}

object DeviceService {

  def init(module: RichModule) = {
    module.serviceOf[DeviceService]("DeviceService")
  }

}