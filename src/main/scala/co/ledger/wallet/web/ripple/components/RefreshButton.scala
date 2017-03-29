package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Component, ComponentDef}

import scala.scalajs.js

/**
  *
  * RefreshButton
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
@Component(ComponentDef(
  selector = "refreshbutton",  // component name (i.e. the HTML tag)
  template = """<img class="refresh-button" ng-class="{spinning: running}" src="images/refresh_button/ic_green_refresh.png"/>""",
  // - or -
  // templateUrl = "counter.html"
  bind = js.Dictionary(
    "running" -> "@" // assign the value of the DOM attribute 'init' to the class property with the same name
  )
))
class RefreshButton {

  def running: Boolean = _running
  def running_=(v: String) = {
    _running = v.toBoolean
  }

  private var _running = false
}

object RefreshButton {

  def init(module: RichModule) = {
    module.componentOf[RefreshButton]
  }

}