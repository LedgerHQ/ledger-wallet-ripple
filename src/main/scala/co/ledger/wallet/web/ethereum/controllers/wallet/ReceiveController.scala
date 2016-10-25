package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import co.ledger.wallet.web.ethereum.services.{SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

/**
  *
  * ReceiveController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 04/05/2016.
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
class ReceiveController(override val windowService: WindowService,
                        override val $scope: Scope,
                        override val sessionService: SessionService) extends Controller with WalletController {

  var address = ""
  var iban = ""
  def uri = {
    if (showIban)
      s"iban:$iban"
    else
      address
  }

  def showIban = currentFormat == "IBAN"
  def showHex = currentFormat == "HEX"

  var formats = js.Dictionary("HEX" -> "HEX", "IBAN" -> "IBAN")
  var currentFormat = sessionService.currentSession.get.sessionPreferences.lift("address_format").getOrElse("HEX")

  sessionService.currentSession.get.wallet.account(0) foreach {(account) =>
    account.ethereumAccount() foreach { (a) =>
      address = a.toChecksumString
      iban = a.toIban
      $scope.$digest()
    }
  }

  def onFormatChanged(format: String): Unit = {
    currentFormat = format
    sessionService.currentSession.get.sessionPreferences("address_format") = format
  }

  def print(address: String): Unit = {
    js.Dynamic.global.print()
  }

  def sendEmail(address: String): Unit = {
    js.Dynamic.global.open(s"mailto:?body=$address")
  }

}

object ReceiveController {
  def init(module: RichModule) = module.controllerOf[ReceiveController]("ReceiveController")
}