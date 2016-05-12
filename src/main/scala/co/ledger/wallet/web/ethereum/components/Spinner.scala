package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.Directive
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}

import scala.scalajs.js

/**
  *
  * Spinner
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 12/05/2016.
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
class Spinner extends Directive {
  import js.Dynamic.{ global => g, newInstance => jsnew }
  override def template: String =
    """
      |<div style="width: 44px; height: 44px">
      |
      |</div>
    """.stripMargin


  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes): Unit = {
    _spinner = jsnew(g.Spinner)(_defaultOptions)
    _spinner.spin(element.find("div").asInstanceOf[JQLite](0))
  }

  private val _defaultOptions = js.Dictionary(
    "lines" -> 9,
    "length" -> 0,
    "width" -> 3,
    "radius" -> 16,
    "corners" -> 0,
    "rotate" -> 0,
    "direction" -> 1,
    "color" -> "#000",
    "speed" -> 0.6,
    "trail" -> 20,
    "shadow" -> false,
    "hwaccel" -> true,
    "className" -> "spinner",
    "zIndex" -> 0,
    "position" -> "relative"
  )

  private val _tinyOptions = js.Dictionary()
  private var _spinner: js.Dynamic = null
}

object Spinner {

  def init(module: RichModule) = module.directiveOf[Spinner]("spinner")

}