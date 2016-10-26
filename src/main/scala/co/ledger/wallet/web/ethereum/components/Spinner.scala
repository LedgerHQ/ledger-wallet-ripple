package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.Directive
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import co.ledger.wallet.web.ethereum.components.Spinner.SpinnerScope

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
import java.util.Date

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, ScalaJSDefined}

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
class Spinner($parse: js.Dynamic) extends Directive {
  import js.Dynamic.{ global => g, newInstance => jsnew }
  import js.timers._

  override type ScopeType = SpinnerScope

  override def template: String =
    """
      |<div style="width: 50px; height: 50px">
      |  <canvas id="canvas" width="50" height="50"></canvas>
      |</div>
    """.stripMargin

  override def postLink(scope: ScopeType, element: JQLite, attrs: Attributes): Unit = {

    val isVisible: js.Function0[Boolean] = {() =>
      var result = js.isUndefined(attrs("ngShow")) && js.isUndefined(attrs("ngHide"))
      if (!js.isUndefined(attrs("ngShow")))
        result =  result || $parse(attrs.apply("ngShow"))(scope).asInstanceOf[Boolean]
      if (!js.isUndefined(attrs("ngHide")))
        result = result || !$parse(attrs.apply("ngHide"))(scope).asInstanceOf[Boolean]
      result
    }

    def startAnimation(): Unit = {
      scope.destroyed = false
      scope.canvas =  element.find("#canvas").asInstanceOf[JQLite](0).asInstanceOf[dom.html.Canvas]
      scope.ctx = scope.canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      scope.startTime = new Date().getTime
      scope.revolutionTime = 1000

      scope.interval = js.Dynamic.global.requestAnimationFrame({() =>
        draw(scope)
      })
      scope.$on("$destroy", {() =>
        scope.destroyed = true
      })
    }
    def startAnimationIfNecessary(): Unit = {
      if (isVisible()) {
        startAnimation()
      } else {
        scope.destroyed = true
      }
    }
    scope.$watch(attrs("ngShow"), {() =>
      startAnimationIfNecessary()
    })
    startAnimationIfNecessary()
  }

  private def draw(scope: ScopeType): Unit = {
    val ctx = scope.ctx
    val canvas = scope.canvas
    val startTime = scope.startTime
    val revolutionTime = scope.revolutionTime

    val width = canvas.width
    val height = canvas.height
    val lineWidth = 3
    val radius = Math.min(width, height) / 2 - lineWidth
    val x = width / 2
    val y = height / 2

    val t = new Date().getTime - startTime

    ctx.clearRect(0, 0, width, height)
    ctx.save()

    // Move registration point to the center of the canvas
    ctx.translate(x, y)

    // Rotate 1 degree
    val e = (t % revolutionTime).toDouble / revolutionTime.toDouble
    def interpolation(e: Double) = -Math.cos(Math.PI/2 * e) + 1
    def easing(): Double = {
      if (e >= 0.5d) {
        (-interpolation(-2 * e + 2) + 2) / 2
      } else {
        interpolation(2 * e) / 2
      }
    }
    ctx.rotate(((easing() * 360) * Math.PI) / 180)

    // Move registration point back to the top left corner of canvas
    ctx.translate(-x, -y)

    ctx.beginPath()
    ctx.arc(x, y, radius, 0, 2 * Math.PI, false)
    ctx.lineWidth = lineWidth
    ctx.strokeStyle = "#CCCCCC"
    ctx.stroke()

    // Cut the circle
    ctx.beginPath()
    ctx.globalAlpha = 1
    ctx.globalCompositeOperation = "destination-out"
    // Cut horizontal rect
    ctx.rect(width / 2, height / 2,  width / 2, height / 2)
    ctx.fill()
    ctx.restore()


    if (!scope.destroyed) {
      js.Dynamic.global.requestAnimationFrame({ () =>
        draw(scope)
      })
    }
  }
}

object Spinner {

  @JSName("SpinnerScope")
  @js.native
  class SpinnerScope extends Scope {
    var destroyed: Boolean = js.native
    var startTime: Long = js.native
    var canvas: dom.html.Canvas = js.native
    var ctx: dom.CanvasRenderingContext2D = js.native
    var interval: js.Any = js.native
    var revolutionTime: Long = js.native
  }

  def init(module: RichModule) = module.directiveOf[Spinner]("spinner")

}