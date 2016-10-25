package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite, Location}
import biz.enef.angulate.{Component, ComponentDef, Directive, Scope}
import co.ledger.wallet.web.ethereum.controllers.onboarding.SelectChainController
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService}

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, Dynamic}

/**
  *
  * NavigationBar
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
/*
@Component(ComponentDef(
  selector = "navigationbar",
  templateUrl = "/templates/components/navigation-bar.html"
))
*/
class NavigationBar(sessionService: SessionService,
                    deviceService: DeviceService,
                    $location: Location,
                    $route: js.Dynamic) extends Directive {
  override type ScopeType = js.Dynamic
  val chains = js.Dictionary(
    "eth" -> "launch.eth",
    "ethc" -> "launch.etc"
  )
  override def templateUrl: String = "/templates/components/navigation-bar.html"

  override def controller(ctrl: ControllerType, scope: Dynamic, elem: JQLite, attrs: Attributes): Unit = {
    scope.items = items
    scope.rightItems = rightItems
    scope.synchronize = synchronize
    scope.isRefreshing = false
    scope.refresh = {() =>
     scope.$eval(attrs.asInstanceOf[js.Dynamic].onClickRefresh)
    }
    scope.chain = {() =>
      sessionService.currentSession.map(_.chain.id).map(chains(_)).getOrElse("")
    }
    scope.switchChain = {() =>
      SelectChainController.resetRemember(deviceService)
      sessionService.stopCurrentSessions()
      $location.url("/onboarding/chain/select")
      $route.reload()
    }
    scope.isSelected = isSelected _
  }

  def isSelected(itemPath: String, currentPath: String) = {
    currentPath.startsWith(itemPath)
  }

  val items = js.Array(
    js.Dictionary(
      "img" -> "images/navigation_bar/ic_home.png",
      "url" -> "#account/0"
    ),
    js.Dictionary(
      "img" -> "images/navigation_bar/ic_send.png",
      "url" -> "#send"
    ),
    js.Dictionary(
      "img" -> "images/navigation_bar/ic_receive.png",
      "url" -> "#receive"
    )
  )

  val rightItems = js.Array(
    js.Dictionary(
      "img" -> "images/navigation_bar/ic_help.png",
      "url" -> "#help"
    )
  )

  val synchronize = js.Dictionary(
    "img" -> "images/refresh_button/ic_white_refresh.png"
  )

  for (item <- items) {
    item("path") = "/" + item("url").substring(1)
  }

  for (item <- rightItems) {
    item("path") = "/" + item("url").substring(1)
  }
}

object NavigationBar {

  def init(module: RichModule) = {
    module.directiveOf[NavigationBar]("navigationBar")
  }

  @js.native
  trait NavigationBarScope extends Scope {
    var isRefreshing: Boolean
  }
}