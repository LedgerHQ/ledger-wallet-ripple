package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.wallet.ethereum.{Ether, EthereumAccount}
import co.ledger.wallet.web.ethereum.components.QrCodeScanner
import co.ledger.wallet.web.ethereum.core.utils.PermissionsHelper
import co.ledger.wallet.web.ethereum.services.{SessionService, WindowService}
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.{Dynamic, timers}
import scala.util.{Failure, Success, Try}

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
class SendIndexController(override val windowService: WindowService,
                          $location: js.Dynamic,
                          sessionService: SessionService,
                          $element: JQLite,
                          $scope: Scope) extends Controller with WalletController{

  var isScanning = false

  var amount = ""
  var gasLimit = 9000
  private var _gasPrice = BigInt("21000000000")
  var gasPrice = new Ether(_gasPrice).toEther.toString()
  var total = Ether(0).toEther.toString()

  sessionService.currentSession.get.sessionPreferences.lift(SendIndexController.RestoreKey) foreach {(state) =>
    val restore = state.asInstanceOf[SendIndexController.RestoreState]
    val dynamicScope = $scope.asInstanceOf[js.Dynamic]
    dynamicScope.address = restore.to
    if (restore.amount.isSuccess)
      dynamicScope.amount = restore.amount.get.toEther.toString()
  }

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

  def computeTotal() = {
    total = getAmountInput().map((amount) => amount + (_gasPrice * 2100)).map(new Ether(_)).getOrElse(Ether(0)).toEther.toString()
  }

  def cancelScanQrCode() = {
    isScanning = false
    scanner.stop()
  }

  def getAmountInput(): Try[BigInt] = {
    Try((BigDecimal($element.find("#amount_input").asInstanceOf[JQLite].`val`().toString) * BigDecimal(10).pow(18)).toBigInt())
  }

  def updateGasPrice(): Unit = {
    import timers._
    sessionService.currentSession.get.wallet.estimatedGasPrice() foreach {(price) =>
      _gasPrice = price.toBigInt
      gasPrice = price.toEther.toString()
      computeTotal()
      setTimeout(0) {
        $scope.$digest()
      }
    }
  }
  updateGasPrice()

  def send() = {
    try {
      val amount = getAmountInput().get
      val recipient = $element.find("#receiver_input").asInstanceOf[JQLite].`val`().toString
      val isIban = true
      val fees = BigDecimal(900000)
      val gasPrice = _gasPrice
      println(s"Amount: $amount")
      println(s"Recipient: $recipient")
      println(s"Is IBAN: $isIban")
      println(s"Fees: $fees")
      val formattedRecipient = recipient
      $location.path(s"/send/$amount/to/$formattedRecipient/from/0/with/$fees/price/$gasPrice")
    } catch {
      case any: Throwable =>
        any.printStackTrace()
        // Display error message
    }
    //
  }

  private val scanner = $element.find("qrcodescanner").scope().asInstanceOf[QrCodeScanner.Controller]
  private val addressInput = $element.find("#receiver_input").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Input]

  scanner.$on("qr-code", { (event: js.Any, value: String) =>
    cancelScanQrCode()
    $scope.$digest()
    Dynamic.global.console.log(value)
    addressInput.value = value
  })

  $scope.$on("$destroy", {() =>
    val amount = Try((BigDecimal($element.find("#amount_input").asInstanceOf[JQLite].`val`().toString) * BigDecimal(10).pow(18)).toBigInt())
    val recipient = $element.find("#receiver_input").asInstanceOf[JQLite].`val`().toString
    sessionService.currentSession.get.sessionPreferences(SendIndexController.RestoreKey) =  SendIndexController.RestoreState(amount.map(new Ether(_)), recipient)
  })
}

object SendIndexController {
  def init(module: RichModule) = module.controllerOf[SendIndexController]("SendIndexController")

  val RestoreKey = "SendIndexController#Restore"
  case class RestoreState(amount: Try[Ether], to: String)
}