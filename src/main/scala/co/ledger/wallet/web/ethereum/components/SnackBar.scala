package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import co.ledger.wallet.web.ethereum.components.SnackBar.{SnackBarInstance, SnackBarScope}
import co.ledger.wallet.web.ethereum.core.utils.JQueryHelper
import co.ledger.wallet.web.ethereum.services.WindowService
import org.scalajs.jquery.jQuery

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

/**
  *
  * SnackBar
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
class SnackBar($translate: js.Dynamic) extends Directive {
  import SnackBar._
  import scala.scalajs.js.timers._
  override def templateUrl: String = "templates/components/snackbar.html"
  override type ScopeType = SnackBarScope

  private def tr(str: String): String = $translate.instant(str).asInstanceOf[String]

  override def controller(ctrl: ControllerType, scope: ScopeType, elem: JQLite, attrs: Attributes): Unit = {
    scope.create = createInstance _
    var currentInstance: SnackBarInstance = null
    val element = jQuery(elem.find("> div").asInstanceOf[JQLite](0))
    scope.isSuccess = () => Option(currentInstance).exists(_.mode == SuccessMode)
    scope.isError = () => Option(currentInstance).exists(_.mode == ErrorMode)
    scope.isNeutral = () => Option(currentInstance).exists(_.mode == NeutralMode)
    scope.title = () => tr(Option(currentInstance).map(_.title).getOrElse(""))
    scope.subtitle = () => tr(Option(currentInstance).map(_.subtitle).getOrElse(""))
    scope.icon = () => Icons(Option(currentInstance).map(_.mode).getOrElse(0))
    scope.dismiss = {() =>
      println("Dismiss")
      element.animate(js.Dictionary("bottom" -> -element.outerHeight(false)), 400, "default", () => {
        currentInstance = null
      })
      ()
    }
    show = (a: Any) => {
      currentInstance = a.asInstanceOf[SnackBarInstance]
      var timeout: js.Any = null
      currentInstance.dismissHandler = () => {
        println("Dismiss delay")
        if (currentInstance == a)
          scope.dismiss()
      }
      setTimeout(0) {
        scope.$digest()
        element.css("bottom", -element.outerHeight(false))
        JQueryHelper.injectCustomEasings()
        element.animate(js.Dictionary("bottom" -> 0), 400, "default", () => {
          if (currentInstance != null) {
            timeout = setTimeout(currentInstance.delay.toMillis) {
              if (currentInstance != null)
                currentInstance.dismissHandler()
            }
          }
        })
      }
      ()
    }
  }

  def createInstance(): SnackBarInstance = {
    new SnackBarInstance(this)
  }

  var show: js.Function1[Any, Unit] = null
  private val Icons = js.Array(
    "images/snackbar/ic_success.png",
    "images/snackbar/ic_failure.png",
    "images/snackbar/ic_success.png"
  )
}

object SnackBar {
  val SuccessMode = 0
  val ErrorMode = 1
  val NeutralMode = 2

  def init(module: RichModule) = {
    module.directiveOf[SnackBar]("snackbar")
  }

  def success(title: String, subtitle: String)(implicit ws: WindowService) = {
    ws.configureSnackBar(SuccessMode, title, subtitle)
  }

  def error(title: String, subtitle: String)(implicit ws: WindowService) = {
    ws.configureSnackBar(ErrorMode, title, subtitle)
  }

  def neutral(title: String, subtitle: String)(implicit ws: WindowService) = {
    ws.configureSnackBar(NeutralMode, title, subtitle)
  }

  class SnackBarInstance(val snackBar: SnackBar) {

    def mode = _mode
    def mode(mode: Int): SnackBarInstance = {
      _mode = mode
      this
    }

    def title = _title
    def title(title: String) = {
      _title = title
      this
    }

    def subtitle = _subtitle
    def subtitle(subtitle: String) = {
      _subtitle = subtitle
      this
    }

    def delay = _delay
    def delay(delay: Duration) = {
      _delay = delay
      this
    }

    def show(): Unit = {
      snackBar.show(this)
    }

    def dismiss(): Unit = {
      if (dismissHandler != null)
        dismissHandler()
    }

    var dismissHandler: Function0[Unit] = null

    private var _mode = 0
    private var _title = ""
    private var _subtitle = ""
    private var _delay: Duration = 3.seconds
  }

  @js.native
  trait SnackBarScope extends Scope {
    var create: js.Function0[SnackBarInstance] =  js.native
    var isSuccess: js.Function0[Boolean] = js.native
    var isError: js.Function0[Boolean] = js.native
    var isNeutral: js.Function0[Boolean] = js.native
    var title: js.Function0[String] = js.native
    var subtitle: js.Function0[String] = js.native
    var icon: js.Function0[String] = js.native
    var dismiss: js.Function0[Unit] = js.native
  }
}
