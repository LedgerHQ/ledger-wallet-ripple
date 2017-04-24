package co.ledger.wallet.web.ripple.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.Location
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.ripple.LedgerApi
import co.ledger.wallet.core.device.ripple.LedgerCommonApiInterface.LedgerApiException
import co.ledger.wallet.core.utils.{DerivationPath, HexUtils, Nullable}
import co.ledger.wallet.core.wallet.ripple.{RippleAccount, XRP}
import co.ledger.wallet.web.ripple.components.{RippleSerializer, SnackBar}
import co.ledger.wallet.web.ripple.services.{DeviceService, RippleLibApiService, SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}

/**
  *
  * SendPerformViewController
  * ledger-wallet-ripple-chrome
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
class SendPerformController(override val windowService: WindowService,
                            override val $scope: Scope,
                            override val sessionService: SessionService,
                            rippleLibApiService: RippleLibApiService,
                            deviceService: DeviceService,
                            $location: Location,
                            $route: js.Dynamic,
                            $routeParams: js.Dictionary[String]) extends Controller with WalletController {
  private var fee: Option[XRP] = None
  if ($routeParams("fee") != "None") {
    fee = Some(XRP($routeParams("fee")))
  }
  private val accountId = $routeParams("account_id").toInt
  private val amount = XRP($routeParams("amount"))
  private val to = RippleAccount("rhGxojnVnEhPQFfMsQ9BK81keWzs6Lzfpv".trim)////RippleAccount($routeParams("recipient").trim)
  println(s"0destination ${to.toString}")
  private val data = $routeParams.lift("data").map(_.replace("0x", "")).map(HexUtils.decodeHex)
  val api = rippleLibApiService.api
  var rippleAccount: RippleAccount = RippleAccount("rhGxojnVnEhPQFfMsQ9BK81keWzs6Lzfpv")
  var derivationPath: DerivationPath = _
  var prepared: String = ""
  var deviceLocal: Device = _
  var tx: js.Dynamic = _
  windowService.disableUserInterface()

  sessionService.currentSession.get.wallet.account(accountId) flatMap {(account) =>
    account.rippleAccount() flatMap  { (rippleAccount_) =>
      rippleAccount = rippleAccount_
      println(s"1ripple account ${rippleAccount.toString}")
      account.rippleAccountDerivationPath()
    } flatMap {(derivationPath_) =>
      derivationPath = derivationPath_
      println(s"2derivation path $derivationPath")
      println(s"3value = ${amount.toXRP.toString}")
      api.preparePayment(api.PaymentParam(
          rippleAccount.toString,
          api.Payment(
            api.Source(rippleAccount.toString, amount = Some(api.LaxAmount(value = Some(amount.toXRP.toString)))),
            api.Destination(to.toString, minAmount = Some(api.LaxAmount(value = Some(amount.toXRP.toString))))
          ),
          Some(api.Instructions(fee = fee.map((fee_) => fee_.toXRP.toString),
                                maxLedgerVersionOffset = Some(400)
          ))
        )
      )
    } flatMap {(prepareResponse) =>
       prepared = prepareResponse.txJSON
      println(s"4prepared $prepared")
      deviceService.lastConnectedDevice()
    } flatMap { (device) =>
      deviceLocal = device
      LedgerApi(device).derivePublicAddress(derivationPath)
    } flatMap { (pubAddressResult) =>
      tx = js.JSON.parse(prepared)
      tx.SigningPubKey = HexUtils.bytesToHex(pubAddressResult.publicKey).toUpperCase
      val stringToSign = JSON.stringify(tx)
      js.Dynamic.global.console.log(tx)
      println(s"5signing $stringToSign")
      LedgerApi(deviceLocal).signTransaction(derivationPath, stringToSign)
    } flatMap {(signed) =>
      tx.TxnSignature = HexUtils.bytesToHex(signed).toUpperCase
      println(s"6submitting")
      val encodedTx = RippleSerializer.encode(JSON.stringify(tx))
      api.submit(new api.SubmitParam(HexUtils.bytesToHex(encodedTx))
      )
    }
  } onComplete {
    case Success(response) =>
      println(s"7success at the end")
      if (response.resultCode == "tesSUCCESS") {
        sessionService.currentSession.get.sessionPreferences.remove(SendIndexController.RestoreKey)
        SnackBar.success("send_perform.completed_title", "send_perform.completed_message").show()
      } else {
        SnackBar.error("send_perform.failed_title", "send_perform.failed_message").show()
      }
      $location.url("/send")
      $route.reload()
    case Failure(ex: LedgerApiException) =>
      ex.printStackTrace()
      SnackBar.error("send_perform.cancelled_title", "send_perform.cancelled_message").show()
      sessionService.currentSession.get.sessionPreferences.remove(SendIndexController.RestoreKey)
      $location.url("/send")
      $route.reload()
    case Failure(ex) =>
      ex.printStackTrace()
      SnackBar.error("send_perform.failed_title", "send_perform.failed_message").show()
      $location.url("/send")
      $route.reload()
  }

  $scope.$on("$destroy", {() =>
    windowService.enableUserInterface()
  })

}

object SendPerformController {
  def init(module: RichModule) = module.controllerOf[SendPerformController]("SendPerformController")
}