package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Controller
import biz.enef.angulate.Module.RichModule
import co.ledger.wallet.web.ethereum.components.SnackBar
import co.ledger.wallet.web.ethereum.services.WindowService

import scala.scalajs.js
import scala.scalajs.js.JSON

/**
  *
  * OperationController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 03/05/2016.
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
class AccountController(override val windowService: WindowService,
                       $routeParams: js.Dictionary[String])
  extends Controller with WalletController {

  println(JSON.stringify($routeParams))

  val accountId = $routeParams("id").toInt

  def refresh(): Unit = {
    println("Refresh now!")
    SnackBar.success("Transaction completed", "Successfully broadcasted to network").show()
    isRefreshing = !isRefreshing
  }

  var isRefreshing = false

  var operations = js.Array(
    js.Dictionary(
      "date" -> "03/18/2016 at 12:45 PM",
      "amount" -> "+1.25104961 Ether",
      "isSend" -> false
    ),
    js.Dictionary(
      "date" -> "03/18/2016 at 8:45 AM",
      "amount" -> "-10.45 Ether",
      "isSend" -> true
    ),
    js.Dictionary(
      "date" -> "03/16/2016 at 12:18 PM",
      "amount" -> "+10.53 Ether",
      "isSend" -> false
    ),
    js.Dictionary(
      "date" -> "03/05/2016 at 13:58 PM",
      "amount" -> "+0.56208423 Ether",
      "isSend" -> false
    ),
    js.Dictionary(
      "date" -> "03/01/2016 at 6:32 PM",
      "amount" -> "+0.14094542 Ether",
      "isSend" -> false
    ),
    js.Dictionary(
      "date" -> "02/16/2016 at 10:25 AM",
      "amount" -> "-3.58437983 Ether",
      "isSend" -> true
    )
  )

}

object AccountController {

  def init(module: RichModule) = {
    module.controllerOf[AccountController]("AccountController")
  }
  
}