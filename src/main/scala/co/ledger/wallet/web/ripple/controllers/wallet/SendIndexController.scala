package co.ledger.wallet.web.ripple.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.ripple.LedgerApi
import co.ledger.wallet.core.device.utils.EventReceiver
import co.ledger.wallet.core.net.HttpException
import co.ledger.wallet.core.wallet.ripple.Wallet.StartSynchronizationEvent
import co.ledger.wallet.core.wallet.ripple.api.ApiAccountRestClient
import co.ledger.wallet.core.wallet.ripple.{RippleAccount, XRP}
import co.ledger.wallet.web.ripple.components.{QrCodeScanner, SnackBar}
import co.ledger.wallet.web.ripple.core.net.JQHttpClient
import co.ledger.wallet.web.ripple.core.utils.PermissionsHelper
import co.ledger.wallet.web.ripple.services.{DeviceService, RippleLibApiService, SessionService, WindowService}
import exceptions.RippleException
import org.scalajs.dom

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.timers.setTimeout
import scala.scalajs.js.{Dynamic, UndefOr, timers}
import scala.util.{Failure, Success, Try}

/**
  *
  * SendIndexController
  * ledger-wallet-ripple-chrome
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
                          val rippleLibApiService: RippleLibApiService,
                          $location: js.Dynamic,
                          $route: js.Dynamic,
                          override val sessionService: SessionService,
                          val deviceService: DeviceService,
                          $element: JQLite,
                          override val $scope: Scope) extends Controller
  with WalletController with EventReceiver{
  var isScanning = false
  var address = ""
  var amount = ""
  var data = ""
  var customFee = "10"
  var tag: String  = ""
  val accountMinimum = 20000000


  var total = XRP.Zero.toBigInt.toString()

  val unit = sessionService.currentSession.get.chain.symbol

  var fee: Option[XRP] = None

  def feeDisplay = fee.getOrElse(XRP.Zero).toXRP

  var isInAdvancedMode = false
  val supportAdvancedMode = true

  sessionService.currentSession.get.wallet.eventEmitter.register(this)

  $scope.$on("$destroy", {() =>
    sessionService.currentSession.foreach(_.wallet.eventEmitter.unregister(this))
  })

  sessionService.currentSession.get.sessionPreferences.lift(SendIndexController.RestoreKey) foreach {(state) =>
    val restore = state.asInstanceOf[SendIndexController.RestoreState]
    address = restore.to
    tag = restore.tag
    customFee = restore.customFee
    isInAdvancedMode = restore.advancedMode
    if (restore.amount.isSuccess)
      amount = restore.amount.get.toXRP.toString()
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
      var a = new XRP(b.toBigInt - fee.getOrElse(XRP.Zero).toBigInt - XRP(accountMinimum).toBigInt) //minus fees
      if (a.toBigInt < 0)
        a = XRP(0)
      amount = a.toXRP.toString()
      computeTotal(false)
      $scope.$apply()
    }
  }

  def computeFees(): Future[Unit] = {
    if (!isInAdvancedMode) {
      println("compute fees")
      _api.fees().map({ (value) => //api.getFee() map {(value) =>
        fee = Some(value)
        val t = getAmountInput().map((amount) => amount + (fee.getOrElse(XRP.Zero).toBigInt)).map(new XRP(_)).getOrElse(XRP(0))
        total = t.toBigInt.toString()
        setTimeout(0) {
          $scope.$digest()
        }
        ()
      }).recover({
        case response: RippleException =>
          SnackBar.error("ripple.down_title", "ripple.down_message").show()
        case ex: Throwable => throw ex
      })
    } else {
      Future.successful()
    }
  }

  override def receive: Receive = {
    case StartSynchronizationEvent() =>
      setTimeout(0) {
        computeFees()
      }
  }

  def computeTotal(fees: Boolean = true): XRP = {
    println("Compute total")
    if (fees == true && !isInAdvancedMode) {
      this.computeFees()
    }
    if (isInAdvancedMode) {
      fee = Some(XRP(customFee.toInt))
    }
    val t = getAmountInput().map((amount) => amount + (fee.getOrElse(XRP.Zero).toBigInt)).map(new XRP(_)).getOrElse(XRP(0))
    total = t.toBigInt.toString()
    t
  }

  def cancelScanQrCode() = {
    isScanning = false
    scanner.stop()
  }

  def getAmountInput(): Try[BigInt] = {
    Try((BigDecimal(amount.replace(',', '.')
      .replace(" ", "")) * BigDecimal(10).pow(6)).toBigInt())
  }

  def getAddressInput(): Try[RippleAccount] = {
    Try(RippleAccount(address))
  }

  def send() = {
    val value = getAmountInput()
    val recipient = getAddressInput()
    if (value.isFailure || value.get == 0) {
      SnackBar.error("send.bad_amount_title", "send.bad_amount_message").show()
    } else if (recipient.isFailure) {
      SnackBar.error("send.bad_address_title", "send.bad_address_message").show()
    } else if (!sessionService.currentSession.get.wallet.isConnected()) {
      SnackBar.error("ripple.down_title", "ripple.down_message").show()
    } else if (isInAdvancedMode && customFee.toInt < 10) {
      SnackBar.error("send.bad_fees_title", "send.bad_fees_message").show()
    } else {
      windowService.disableUserInterface()
      sessionService.currentSession.get.wallet.balance() map {(exists) =>
        println("exist", exists)
        if ((exists == XRP.Zero) && value.get<accountMinimum) {
          SnackBar.error("send.bad_amount_for_address_creation_title", "send.bad_amount_for_address_creation_message").show()
        } else {
          println(s"Amount: $amount")
          println(s"Recipient: $address")
          println(s"Fee: $fee")
          println(s"Tag: $tag")
          sessionService.currentSession.get.wallet.balance() foreach {
            (balance) =>
              if (computeTotal(false) > balance - XRP(accountMinimum)) {
                SnackBar.error("send.insufficient_funds_title", "send.insufficient_funds_message").show()
              } else {
                deviceService.lastConnectedDevice().flatMap(LedgerApi(_).getAppConfiguration()) foreach { (conf) =>
                  if (data.nonEmpty && !conf.isArbitraryDataSignatureEnabled) {
                    SnackBar.error("send.enable_data_title", "send.enable_data_message").show()
                  } else {
                    var tagInt = ""
                    if (tag != "") {
                      tagInt = "/" ++ tag
                    }
                    println(s"/send/${value.get.toString()}/to/$address/from/0/with/${fee.getOrElse(12)}/tag$tagInt")
                    $location.path(s"/send/${value.get.toString()}/to/$address/from/0/with/${fee.getOrElse(12)}/tag$tagInt")
                    $scope.$apply()
                  }
                }
              }
          }
        }
      }
    } onComplete {
      case result =>
        windowService.enableUserInterface()
        result match {
          case Failure(ex: RippleException) =>
            SnackBar.error("ripple.down_title", "ripple.down_message").show()

          case Failure(any: Throwable) =>
            windowService.enableUserInterface()
            any.printStackTrace()
            SnackBar.error("send.network_unavailable_title", "send.network_unavailable_message").show()

          case all =>
        }
    }
  }
  private val scanner = $element.find("qrcodescanner").scope().asInstanceOf[QrCodeScanner.Controller]

  scanner.$on("qr-code", { (event: js.Any, value: String) =>
    cancelScanQrCode()
    address = value.replace("iban:", "")
    $scope.$apply()
  })
  $scope.$on("$destroy", {() =>
    val amount = Try((BigDecimal($element.find("#amount_input").asInstanceOf[JQLite].`val`().toString) * BigDecimal(10).pow(6)).toBigInt())
    val recipient = $element.find("#receiver_input").asInstanceOf[JQLite].`val`().toString
    sessionService.currentSession.get.sessionPreferences(SendIndexController.RestoreKey) =  SendIndexController.RestoreState(
      amount.map(new XRP(_)),
      recipient,
      customFee,
      tag,
      isInAdvancedMode
    )
  })
  private val _api = new ApiAccountRestClient(JQHttpClient.xrpInstance)

  computeTotal()
}

object SendIndexController {
  def init(module: RichModule) = module.controllerOf[SendIndexController]("SendIndexController")
  val RestoreKey = "SendIndexController#Restore"
  case class RestoreState(amount: Try[XRP], to: String, customFee: String, tag: String, advancedMode: Boolean)
}
