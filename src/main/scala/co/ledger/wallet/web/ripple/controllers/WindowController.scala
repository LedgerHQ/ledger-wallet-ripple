package co.ledger.wallet.web.ripple.controllers

import autoupdater.Updater
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import co.ledger.wallet.core.device.utils.EventReceiver
import co.ledger.wallet.web.ripple.Application
import co.ledger.wallet.web.ripple.components.NavigationBar.NavigationBarScope
import co.ledger.wallet.web.ripple.components.SnackBar.SnackBarScope
import co.ledger.wallet.web.ripple.services.WindowService

import scala.scalajs.js
import scala.scalajs.js.timers
import scala.scalajs.js.timers._


/**
  *
  * WindowController
  * ledger-wallet-ripple-chrome
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
class WindowController(windowService: WindowService, $scope: Scope, $element: JQLite, $document: JQLite) extends Controller with EventReceiver {
  import timers._
  var showNavigationBar = false

  // Disable backspace
  $document.on("keydown", {(e: js.Dynamic) =>
    if (e.which.asInstanceOf[Double] == 8 && ( e.target.nodeName.asInstanceOf[String] != "INPUT" && e.target.nodeName.asInstanceOf[String] != "SELECT" ) ){
      e.preventDefault()
    }
  })

  def refresh(): Unit = {
    windowService.notifyRefresh()
  }

  windowService onNavigationBarVisibilityChanged {(isVisible) =>
    showNavigationBar = isVisible
    setTimeout(0) {
      _navigationBarScope.$apply()
    }
  }

  windowService.configureSnackBar = {(mode: Int, title: String, subtitle: String) =>
    _snackBarScope.create().mode(mode).title(title).subtitle(subtitle)
  }

  var isUiEnabled = true

  windowService.onUserInterfaceEnableChanged {(enable) =>
    isUiEnabled = enable
    setTimeout(0) {
      $scope.$parent.$apply()
    }
  }

  override def receive: Receive = {
    case windowService.StartRefresh() =>
      _navigationBarScope.isRefreshing = true
      setTimeout(0) {
        _navigationBarScope.$apply()
      }
    case windowService.StopRefresh() =>
      _navigationBarScope.isRefreshing = false
      setTimeout(0) {
        _navigationBarScope.$apply()
      }
  }

  $scope.$on("$destroy", {(_: js.Any) =>
    windowService.eventEmitter.unregister(this)
  })
  windowService.eventEmitter.register(this)

  private val _snackBarScope = $element.find("> snackbar").scope().asInstanceOf[SnackBarScope]
  private val _navigationBarScope = $element.find("> navigation-bar").scope().asInstanceOf[NavigationBarScope]
}

object WindowController {

  def init(module: RichModule) = {
    module.controllerOf[WindowController]("WindowController")
  }

}