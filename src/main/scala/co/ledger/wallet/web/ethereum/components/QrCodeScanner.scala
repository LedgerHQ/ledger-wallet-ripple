package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.ext.Color
import org.scalajs.dom.raw.Event

import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.Dynamic
import scala.scalajs.js.timers._

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
  override type ScopeType = QrCodeScanner.Controller

  override def controller(ctrl: ControllerType, scope: ScopeType, elem: JQLite, attrs: Attributes): Unit = {
    scope.start = start _
    scope.stop = stop _
    scope.width = 370
    scope.height = 240
    scope.asInstanceOf[Scope].$on("$destroy", () => {
      stop()
    })
    _scope = scope.asInstanceOf[QrCodeScanner.Controller]
  }

  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes, controller: ControllerType): Unit = {
    _video = element.find("video").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Video]
    _overlay = element.find("#overlay").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Canvas]
    _buffer = element.find("#buffer").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Canvas]
  }

  def start(): Unit = {
    if (!_running) {
      _running = true
      js.Dynamic.global.navigator.webkitGetUserMedia(js.Dictionary("video" -> true), (stream: js.Dynamic) => {
         _stream = stream
        _video.src = js.Dynamic.global.URL.createObjectURL(stream).toString
        _video.play()
        _video.onplaying = {(event: Event) =>
          _video.onplaying = null
          scheduleQrCodeDecoder()
        }
        draw(_overlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D])
        if (!_running)
          performStop()
      }, (error: js.Object) => {

      })
    }
  }

  def stop(): Unit = {
    if (_running) {
      _running = false
      performStop()
    }
  }

  private def clearRenderingBuffer(): Unit = {
    val ctx = _buffer.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.clearRect(0, 0, width, height)
  }

  private def performStop(): Unit = {
    if (_stream != null) {
      _stream.getTracks().asInstanceOf[js.Array[js.Dynamic]](0).stop()
      _stream = null
      _video.pause()
      _video.src = ""
      _video.load()
      clearRenderingBuffer()
    }
  }

  private def draw(ctx: dom.CanvasRenderingContext2D) = {
    val w = width
    val h = height

    val frameWidth = 170
    val frameHeight = 170
    val frameX = (w - frameWidth) / 2
    val frameY = (h - frameHeight) / 2
    val borderSize = 5
    val borderLength = 30

    // Draw the black overlay
    ctx.save()

    ctx.clearRect(0, 0, w, h)

    ctx.beginPath()
    ctx.fillStyle = Color(0, 0, 0).toString()
    ctx.globalAlpha = 0.30
    ctx.rect(0, 0, w, h)
    ctx.fill()

    // Draw crosshair
    ctx.beginPath()
    //ctx.globalCompositeOperation = "destination-out"
    ctx.beginPath()
    ctx.fillStyle = Color(255, 255, 255).toString()
    ctx.globalAlpha = 0.75
    roundedRect(ctx, frameX, frameY, frameWidth, frameHeight)
    ctx.fill()

    // Cut the crosshair
    ctx.beginPath()
    ctx.globalAlpha = 1
    ctx.globalCompositeOperation = "destination-out"
    roundedRect(ctx, frameX + borderSize, frameY + borderSize, frameWidth - borderSize * 2, frameHeight - borderSize * 2)
    // Cut horizontal rect
    ctx.rect(frameX + borderLength, frameY, frameWidth - borderLength * 2, frameHeight)
    // Cut vertical rect
    ctx.rect(frameX, frameY + borderLength, frameWidth, frameHeight - borderLength * 2)
    ctx.fill()

    ctx.restore()
  }

  private def roundedRect(ctx: dom.CanvasRenderingContext2D, x: Int, y: Int, w: Int, h: Int, radius: Int = 5): Unit = {
    ctx.save()
    ctx.beginPath()
    ctx.moveTo(x + radius, y)
    ctx.lineTo(x + w - radius, y)
    ctx.quadraticCurveTo(x + w, y, x + w, y + radius)
    ctx.lineTo(x + w, y + h - radius)
    ctx.quadraticCurveTo(x + w, y + h, x + w - radius, y + h)
    ctx.lineTo(x + radius, y + h)
    ctx.quadraticCurveTo(x, y + h, x, y + h - radius)
    ctx.lineTo(x, y + radius)
    ctx.quadraticCurveTo(x, y, x + radius, y)
    ctx.closePath()
    ctx.fill()
    ctx.restore()
  }

  private def scheduleQrCodeDecoder(): Unit = {
    setTimeout(1000) {
      val context = _buffer.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
      context.drawImage(_video, 0, 0, width, height)
      val result = Dynamic.global.zbarProcessImageData(context.getImageData(0, 0, width, height)).asInstanceOf[js.Array[js.Dynamic]]
      if (result.length > 0 && result(0).asInstanceOf[js.Array[js.Dynamic]](0).asInstanceOf[String] == "QR-Code") {
        val qrCode = result(0).asInstanceOf[js.Array[js.Dynamic]](2).asInstanceOf[String]
        _scope.$emit("qr-code", qrCode)
        stop()
      }
      else if (_running)
        scheduleQrCodeDecoder()
    }
  }

  def width = _overlay.width
  def height = _overlay.height

  private var _running = false
  private var _video: dom.html.Video = null
  private var _overlay: dom.html.Canvas = null
  private var _buffer: dom.html.Canvas = null
  private var _stream: js.Dynamic = null
  private var _scope: QrCodeScanner.Controller = null
}

object QrCodeScanner {

  @js.native
  trait Controller extends Scope {
    var start: js.Function0[Unit] = js.native
    var stop: js.Function0[Unit] = js.native
    var width: Int = js.native
    var height: Int = js.native
  }

  def init(module: RichModule) = module.directiveOf[QrCodeScanner]("qrcodescanner")
}