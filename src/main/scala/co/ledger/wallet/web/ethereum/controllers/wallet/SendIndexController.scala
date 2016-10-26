package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.wallet.ethereum.{Ether, EthereumAccount}
import co.ledger.wallet.web.ethereum.components.{QrCodeScanner, SnackBar}
import co.ledger.wallet.web.ethereum.core.utils.PermissionsHelper
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService, WindowService}
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
                          $route: js.Dynamic,
                          override val sessionService: SessionService,
                          val deviceService: DeviceService,
                          $element: JQLite,
                          override val $scope: Scope) extends Controller with WalletController{

  var isScanning = false

  var address = ""
  var amount = ""
  var customGasLimit = ""
  var data = ""

  def gasLimit = if (!isInAdvancedMode) BigInt(21000) else Try(BigInt(customGasLimit)).getOrElse(BigInt(21000))
  private var _gasPrice = BigInt("21000000000")
  var gasPrice = _gasPrice.toString()
  var total = Ether(0).toBigInt.toString()
  val unit = sessionService.currentSession.get.chain.symbol

  var isInAdvancedMode = false
  val supportAdvancedMode = sessionService.currentSession.get.dongleAppVersion > "1.0.0"

  sessionService.currentSession.get.sessionPreferences.lift(SendIndexController.RestoreKey) foreach {(state) =>
    val restore = state.asInstanceOf[SendIndexController.RestoreState]
    address = restore.to
    customGasLimit = restore.customGasLimit
    isInAdvancedMode = restore.advancedMode
    data = restore.data
    if (restore.amount.isSuccess)
      amount = restore.amount.get.toEther.toString()
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

  def max(): Unit = {
    sessionService.currentSession.get.wallet.balance() foreach {(b) =>
      var a = new Ether(b.toBigInt - (_gasPrice * gasLimit))
      if (a.toBigInt < 0)
        a = Ether(0)
      amount = a.toEther.toString()
      computeTotal()
      $scope.$apply()
    }
  }

  def computeTotal(): Ether = {
    val t = getAmountInput().map((amount) => amount + (_gasPrice * gasLimit)).map(new Ether(_)).getOrElse(Ether(0))
    total = t.toBigInt.toString()
    t
  }

  def cancelScanQrCode() = {
    isScanning = false
    scanner.stop()
  }

  def getAmountInput(): Try[BigInt] = {
    Try((BigDecimal(amount.replace(',', '.').replace(" ", "")) * BigDecimal(10).pow(18)).toBigInt())
  }

  def getAddressInput(): Try[EthereumAccount] = {
    Try(EthereumAccount(address))
  }

  def updateGasPrice(): Unit = {
    import timers._
    sessionService.currentSession.get.wallet.estimatedGasPrice() foreach {(price) =>
      _gasPrice = price.toBigInt
      gasPrice = price.toBigInt.toString()
      computeTotal()
      setTimeout(0) {
        $scope.$digest()
      }
    }
  }

  updateGasPrice()

  def send() = {
    try {
      val value = getAmountInput()
      val recipient = getAddressInput()
      if (value.isFailure) {
        SnackBar.error("send.bad_amount_title", "send.bad_amount_message").show()
      } else if (recipient.isFailure) {
        SnackBar.error("send.bad_address_title", "send.bad_address_message").show()
      } else if (isInAdvancedMode && Try(BigInt(customGasLimit)).isFailure) {
        SnackBar.error("send.bad_limit_title", "send.bad_limit_message").show()
      } else {
        val isIban = true
        val fees = gasLimit
        val gasPrice = _gasPrice
        println(s"Amount: $amount")
        println(s"Recipient: $address")
        println(s"Is IBAN: $isIban")
        println(s"Gas limit: $fees")
        println(s"Data: $data")
        sessionService.currentSession.get.wallet.balance() foreach {(balance) =>
          if (computeTotal() > balance) {
            SnackBar.error("send.insufficient_funds_title", "send.insufficient_funds_message").show()
          } else {
            deviceService.lastConnectedDevice().flatMap(LedgerApi(_).getAppConfiguration()) foreach {(conf) =>
              if (data.nonEmpty && !conf.isArbitraryDataSignatureEnabled) {
                SnackBar.error("send.enable_data_title", "send.enable_data_message").show()
              } else {
                println(s"/send/${value.get.toString()}/to/$address/from/0/with/$fees/price/$gasPrice/data/$data")
                $location.path(s"/send/${value.get.toString()}/to/$address/from/0/with/$fees/price/$gasPrice/data/$data")
                $scope.$apply()
              }
            }
          }
        }
      }
    } catch {
      case any: Throwable =>
        any.printStackTrace()
        // Display error message
    }
    //
  }

  private val scanner = $element.find("qrcodescanner").scope().asInstanceOf[QrCodeScanner.Controller]

  scanner.$on("qr-code", { (event: js.Any, value: String) =>
    cancelScanQrCode()
    address = value.replace("iban:", "")
    $scope.$apply()
  })

  $scope.$on("$destroy", {() =>
    val amount = Try((BigDecimal($element.find("#amount_input").asInstanceOf[JQLite].`val`().toString) * BigDecimal(10).pow(18)).toBigInt())
    val recipient = $element.find("#receiver_input").asInstanceOf[JQLite].`val`().toString
    sessionService.currentSession.get.sessionPreferences(SendIndexController.RestoreKey) =  SendIndexController.RestoreState(
      amount.map(new Ether(_)),
      recipient,
      customGasLimit,
      data,
      isInAdvancedMode
    )
  })
}

object SendIndexController {
  def init(module: RichModule) = module.controllerOf[SendIndexController]("SendIndexController")

  val RestoreKey = "SendIndexController#Restore"
  case class RestoreState(amount: Try[Ether], to: String, customGasLimit: String, data: String, advancedMode: Boolean)
}