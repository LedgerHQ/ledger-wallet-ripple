package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Component, ComponentDef}

import scala.scalajs.js

/**
  *
  * ProgressBar
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
@Component(ComponentDef(
  template =
    """
      |<div class="progressbar" >
      | <div style="width: {{width}}px">
      |   <div class="left" style="width: {{percent}}%" ng-class="{animated: animated}"></div>
      | </div>
      | <div class="regular-text-small">{{percent}}%</div>
      |</div>
    """.stripMargin,
  selector = "progressbar",
  bind = js.Dictionary(
    "progress" -> "@",
    "total" -> "@",
    "percent" -> "@",
    "width" -> "@",
    "animated" -> "@"
  )
))
class ProgressBar {

  var width = 300
  var animated = true

  def percent_=(percent: String): Unit = {
    _total = 100
    _progress = percent.toInt
  }
  def percent = Math.min((_progress * 100) / _total, 100)

  private var _total = 100
  private var _progress = 0

}

object ProgressBar {
  def init(module: RichModule) = module.componentOf[ProgressBar]
}
