package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Component, ComponentDef}

import scala.scalajs.js

/**
  *
  * LButton
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 06/05/2016.
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
  selector = "lbutton",
  template =
    """
      |<a class='lbutton' ng-class="{green: type == 'validate', disabled: disabled, grey: type == 'cancel', small: small}">
      | <i class="fa fa-{{icon}}"></i><span>{{text}}</span>
      |</a>
      |""".stripMargin,
  bind = js.Dictionary(
    "icon" -> "@",
    "text" -> "@",
    "type" -> "@",
    "disabled" -> "@",
    "href" -> "@",
    "small" -> "@"
  )
))
class LButton {
  var text = ""
  var icon = ""
  var `type` = "validate"

  def disabled = _disabled
  def disabled_=(v: String) = _disabled = v.toBoolean
  private var _disabled = false
}

object LButton {

  def init(module: RichModule) = {
    module.componentOf[LButton]
  }

}