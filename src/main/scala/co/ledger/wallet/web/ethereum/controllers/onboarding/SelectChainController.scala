package co.ledger.wallet.web.ethereum.controllers.onboarding

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.{JQLite, Location}
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.web.ethereum.core.utils.ChromeGlobalPreferences
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  * SelectChainController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 24/10/2016.
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
class SelectChainController(override val windowService: WindowService,
                            deviceService: DeviceService,
                            $location: Location,
                            $route: js.Dynamic,
                            sessionService: SessionService,
                            $scope: Scope,
                            $element: JQLite,
                            $routeParams: js.Dictionary[String])
  extends Controller with OnBoardingController {




  def select(chain: String) = {
    preferences foreach {(prefs) =>
      prefs
        .edit()
        .putString("chain", chain)
        .putBoolean("remember", $element.find("#remember").asInstanceOf[js.Dynamic].is(":checked").asInstanceOf[Boolean])
        .commit()
      $location.url(s"/onboarding/opening/$chain/")
      $route.reload()
    }
  }

  private def preferences = SelectChainController.preferences(deviceService)

  preferences foreach {(prefs) =>
    if (prefs.boolean("remember").getOrElse(false))
      select(prefs.string("chain").get)
  }

/*
 $location.url(s"/onboarding/opening/${currentChain}/")
          $route.reload()
 */

}

object SelectChainController {

  def preferences(deviceService: DeviceService) = {
    deviceService.lastConnectedDevice() flatMap {(device) =>
      LedgerApi(device).walletIdentifier()
    } map {(identifier) =>
      new ChromeGlobalPreferences(s"$identifier chain")
    }
  }

  def getRememberedChain(deviceService: DeviceService): Future[Option[String]]= {
    preferences(deviceService) map {(prefs) =>
      prefs.boolean("remember").flatMap {(remember) =>
        if (remember)
          prefs.string("chain")
        else
          None
      }
    }
  }

  def resetRemember(deviceService: DeviceService): Unit = {
    preferences(deviceService) foreach {(prefs) =>
      prefs.edit().remove("remember").commit()
    }
  }

  def init(module: RichModule) = module.controllerOf[SelectChainController]("SelectChainController")
}