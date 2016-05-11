package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.web.ethereum.components.QrCodeScanner
import co.ledger.wallet.web.ethereum.core.utils.PermissionsHelper
import co.ledger.wallet.web.ethereum.services.WindowService
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.util.{Failure, Success}

/**
  *
  * SendIndexController
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
class SendIndexController(override val windowService: WindowService, $location: js.Dynamic, $element: JQLite, $scope: Scope) extends Controller with WalletController{

  var isScanning = false

  def scanQrCode() = {
    PermissionsHelper.requestIfNecessary("videoCapture") map {(hasPermission) =>
      if (!hasPermission)
        throw new Exception("No Video capture permission")
      ()
    } onComplete {
      case Success(_) =>
        isScanning = true
        scanner.start()
        $scope.$digest()
      case Failure(_) =>

    }
  }

  def cancelScanQrCode() = {
    isScanning = false
    scanner.stop()
  }

  def send() = {
    try {
      val amount = BigDecimal($element.find("#amount_input").asInstanceOf[JQLite].`val`().toString)
      val recipient = $element.find("#receiver_input").asInstanceOf[JQLite].`val`().toString
      val isIban = true
      val fees = BigDecimal(0)
      println(s"Amount: $amount")
      println(s"Recipient: $recipient")
      println(s"Is IBAN: $isIban")
      println(s"Fees: $fees")
      val formattedRecipient = recipient
      $location.path(s"/send/$amount/to/$formattedRecipient/from/0/with/$fees")
    } catch {
      case any: Throwable =>
        any.printStackTrace()
        // Display error message
    }
    //
  }

  private val scanner = $element.find("qrcodescanner").scope().asInstanceOf[QrCodeScanner.Controller]
  private val addressInput = $element.find("#receiver_input").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Input]
  scanner.$on("qr-code", {(event: js.Any, value: String) =>
    cancelScanQrCode()
    $scope.$digest()
    Dynamic.global.console.log(value)
    addressInput.value = value
  })
}

object SendIndexController {
  def init(module: RichModule) = module.controllerOf[SendIndexController]("SendIndexController")
}