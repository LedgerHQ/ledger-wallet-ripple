package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.Directive
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import org.scalajs.dom

import scala.scalajs.js

/**
  *
  * QrCodeScanner
  * ledger-wallet-ethereum-chrome
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
class QrCodeScanner extends Directive {
  override def templateUrl: String = "templates/components/qrcode_scanner.html"

  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes, controller: ControllerType): Unit = {
    val video = element.find("video").asInstanceOf[JQLite]
    js.Dynamic.global.navigator.webkitGetUserMedia(js.Dictionary("video" -> true), (stream: js.Object) => {
      println("Got a " + stream)
    }, (error: js.Object) => {

    })
  }
}

object QrCodeScanner {
  def init(module: RichModule) = module.directiveOf[QrCodeScanner]("qrcodescanner")
}