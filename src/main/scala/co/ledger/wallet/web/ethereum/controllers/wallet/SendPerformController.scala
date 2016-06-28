package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ethereum.EthereumAccount
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService}

import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * SendPerformViewController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 10/05/2016.
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
class SendPerformController($scope: Scope,
                            sessionService: SessionService,
                            deviceService: DeviceService,
                            $routeParams: js.Dictionary[String]) extends Controller {
  import SendPerformController._

  js.Dynamic.global.console.log($routeParams)
  def isInProgressionMode = _currentMode == ProgressionMode
  def isInWaitingMode = _currentMode == WaitingMode

  def switchMode() = {
    if (isInProgressionMode)
      _currentMode = WaitingMode
    else
      _currentMode = ProgressionMode
  }

  private val startGas = BigInt($routeParams("fees"))
  private val gasPrice = BigInt($routeParams("price"))
  private val accountId = $routeParams("account_id").toInt
  private val amount = BigInt($routeParams("amount"))
  private val to = EthereumAccount($routeParams("recipient"))

  sessionService.currentSession.get.wallet.account(accountId) flatMap {(account) =>
    account.freshEthereumAccount()
  } flatMap {(from) =>
    deviceService.lastConnectedDevice() flatMap {(device) =>
      LedgerApi(device).signTransaction(
        BigInt(1),
        gasPrice,
        startGas,
        DerivationPath("44'/60'/0'/0"),
        to,
        amount,
        Array.empty[Byte]
      )
    }
  } flatMap {(signature) =>
    sessionService.currentSession.get.wallet.pushTransaction(signature.signedTx)
  } onComplete {
    case Success(_) =>

    case Failure(ex) =>
      ex.printStackTrace()
  }

  /*
  private def iterate(): Unit = {
  import scala.scalajs.js.timers._
    setTimeout(500) {
      progression = progression + 1
      $scope.$digest()
      println(s"Progression $progression")
      iterate()
    }
  }
  */

  var progression = 0
  private var _currentMode = WaitingMode

}

object SendPerformController {

  val ProgressionMode = 0
  val WaitingMode = 1

  def init(module: RichModule) = module.controllerOf[SendPerformController]("SendPerformController")

}