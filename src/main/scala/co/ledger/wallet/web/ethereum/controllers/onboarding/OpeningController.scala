package co.ledger.wallet.web.ethereum.controllers.onboarding

import java.util.Date

import biz.enef.angulate.Controller
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{JQLite, Location}
import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.ethereum.{LedgerApi, LedgerBolosApi, LedgerDerivationApi}
import co.ledger.wallet.core.device.ethereum.LedgerDerivationApi.PublicAddressResult
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ethereum.EthereumAccount
import co.ledger.wallet.core.wallet.ethereum.Wallet.WalletNotSetupException
import co.ledger.wallet.web.ethereum.core.utils.ChromePreferences
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, duration}
import scala.scalajs.js
import scala.util.{Failure, Success}
import duration._
import scala.scalajs.js.timers

/**
  *
  * OpeningController
  * ledger-wallet-ethereum-chrome
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
                        $element: JQLite)
  extends Controller with OnBoardingController {

  deviceService.lastConnectedDevice() map {(device) =>
    LedgerApi(device)
  } flatMap {(api) =>
    sessionService.startNewSessions(api)
  } flatMap {(_) =>
    ChromePreferences.load(sessionService.currentSession.get.name, sessionService.currentSession.get.password)
  } flatMap { (_) =>
      sessionService.currentSession.get.wallet.balance().map({(balance) =>
        sessionService.currentSession.get.sessionPreferences("balance_cache") = balance.toBigInt.toString()
      })
  } flatMap { (_) =>
    sessionService.currentSession.get.wallet.mostRecentBlock() flatMap { (block) =>
      val now = new Date()
      if (now.getTime - block.time.getTime >= 7.days.toMillis) {
        synchronizeWallet()
      } else {
        synchronizeWallet()
        Future.successful()
      }
    } recoverWith {
      case walletNotSetup: WalletNotSetupException =>
        synchronizeWallet()
      case others => throw others
    }
  } onComplete {
    case Success(_) =>
      $location.url("/account/0")
      $route.reload()
    case Failure(ex) => ex.printStackTrace()
  }

  def synchronizeWallet(): Future[Unit] = {
    sessionService.currentSession.get.wallet.synchronize()
  }

}

object OpeningController {
  def init(module: RichModule) = module.controllerOf[OpeningController]("OpeningController")
}