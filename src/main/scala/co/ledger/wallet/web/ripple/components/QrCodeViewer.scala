package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import biz.enef.angulate.{Directive, Scope}

import scala.scalajs.js
import scala.scalajs.js.{Dynamic, UndefOr}
import scala.scalajs.js.Dynamic.{global => g, newInstance => jsnew}

/**
  *
  * QrCodeViewer
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 09/05/2016.
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

class QrCodeViewer extends Directive {
  override type ScopeType = Scope
  override type ControllerType = js.Dynamic

  val DefaultWidth = 138
  val DefaultHeight = 138
  val DefaultDarkColor = "#000000"
  val DefaultLightColor = "#ffffff"
  val DefaultCorrectionLevel = g.QRCode.CorrectLevel.H

  override def template: String =
    """
      |<div class="qrcode-viewer-frame">
      | <div id="qrcode_container"></div>
      |</div>
    """.stripMargin
  override val controllerAs = "directive"

  override def controller(ctrl: Dynamic, scope: Scope, elem: JQLite, attrs: Attributes): Unit = {
    ctrl.qrCode = jsnew(g.QRCode)(elem.find("#qrcode_container").asInstanceOf[JQLite](0), js.Dictionary(
      "text" -> attrs("value"),
      "width" -> attrs("width").map(_.toInt).getOrElse(DefaultWidth),
      "height" -> attrs("height").map(_.toInt).getOrElse(DefaultHeight),
      "colorDark" ->attrs("dark").getOrElse(DefaultDarkColor),
      "colorLight" -> attrs("light").getOrElse(DefaultLightColor),
      "correctLevel" -> attrs("correctLevel").map(_.toInt).getOrElse(DefaultCorrectionLevel)
    ))
  }

  override def postLink(scope: Scope, element: JQLite, attrs: Attributes, controller: Dynamic): Unit = {
   scope.$watch(attrs("value"), {(newValue: UndefOr[String]) =>
     controller.qrCode.makeCode(newValue.getOrElse("").toString)
   })
  }

}

object QrCodeViewer {
  def init(module: RichModule) = {
    module.directiveOf[QrCodeViewer]("qrcodeviewer")
  }


}