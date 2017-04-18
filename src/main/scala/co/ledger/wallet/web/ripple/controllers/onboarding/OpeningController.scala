package co.ledger.wallet.web.ripple.controllers.onboarding

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{JQLite, Location}
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.ripple.LedgerApi
import co.ledger.wallet.web.ripple.core.utils.ChromePreferences
import co.ledger.wallet.web.ripple.services.{DeviceService, SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  *
  * OpeningController
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 12/05/2016.
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
class OpeningController(override val windowService: WindowService,
                        deviceService: DeviceService,
                        $location: Location,
                        $route: js.Dynamic,
                        sessionService: SessionService,
                        $scope: Scope,
                        $element: JQLite,
                        $routeParams: js.Dictionary[String])
  extends Controller with OnBoardingController {

  var isInErrorMode = false
  deviceService.lastConnectedDevice() map {(device) =>
    LedgerApi(device)
  } flatMap {(api) =>
    println("chain")
    val chain = SessionService.RippleChain()
    sessionService.startNewSessions(api, chain)
  } flatMap {(_) =>
    println("session started")
    ChromePreferences.load(sessionService.currentSession.get.name, sessionService.currentSession.get.password)
  } flatMap { (_) =>
    println("chrome init")
    sessionService.currentSession.get.wallet.balance().map({(balance) =>
        print("got balance")
        sessionService.currentSession.get.sessionPreferences("balance_cache") = balance.toBigInt.toString()
      })
  } flatMap { (_) =>
    println("fghgfhfghgf")
    synchronizeWallet()
    Future.successful()
  } onComplete {
    case Success(_) =>
      $location.url("/account/0")
      $route.reload()
    case Failure(ex) =>
      ex.printStackTrace()
      isInErrorMode = true
      $scope.$apply()
  }

  def synchronizeWallet(): Future[Unit] = {
    sessionService.currentSession.get.wallet.synchronize()
  }

  def openHelpCenter(): Unit = {
    js.Dynamic.global.open("http://support.ledgerwallet.com/help_center")
  }

}

object OpeningController {
  def init(module: RichModule) = module.controllerOf[OpeningController]("OpeningController")
}