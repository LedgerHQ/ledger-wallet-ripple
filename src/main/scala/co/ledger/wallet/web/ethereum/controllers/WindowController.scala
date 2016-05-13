package co.ledger.wallet.web.ethereum.controllers

import biz.enef.angulate.Controller
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import co.ledger.wallet.web.ethereum.Application
import co.ledger.wallet.web.ethereum.components.SnackBar.SnackBarScope
import co.ledger.wallet.web.ethereum.services.WindowService

/**
  *
  * WindowController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 02/05/2016.
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
class WindowController(windowService: WindowService, $element: JQLite) extends Controller {

  var showNavigationBar = false

  windowService onNavigationBarVisibilityChanged {(isVisible) =>
    showNavigationBar = isVisible
  }


  windowService.configureSnackBar = {(mode: Int, title: String, subtitle: String) =>
    _snackBarScope.create().mode(mode).title(title).subtitle(subtitle)
  }

  private val _snackBarScope = $element.find("> snackbar").scope().asInstanceOf[SnackBarScope]

}

object WindowController {

  def init(module: RichModule) = {
    module.controllerOf[WindowController]("WindowController")
  }

}