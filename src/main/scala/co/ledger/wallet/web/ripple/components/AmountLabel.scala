package co.ledger.wallet.web.ripple.components

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import co.ledger.wallet.core.wallet.ripple.XRP
import co.ledger.wallet.web.ripple.components.AmountLabel.AmountLabelScope
import co.ledger.wallet.web.ripple.core.utils.XRPFormatter
import co.ledger.wallet.web.ripple.services.SessionService

import scala.scalajs.js
import scala.scalajs.js.Dictionary

/**
  *
  * AmountLabel
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 13/07/2016.
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
class AmountLabel($locale: js.Dynamic, $translate: js.Dynamic, sessionService: SessionService) extends Directive {
  override def templateUrl: String = "templates/components/amount.html"

  override type ScopeType = AmountLabelScope

  override def postLink(scope: AmountLabelScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch("rawValue", {(value: js.Any) =>
      if (scope.rawValue == null)
        scope.rawValue = ""
      scope.value = XRPFormatter.format(XRP(if (scope.rawValue.isEmpty) "1000000000000000000" else scope.rawValue), unit(attrs), js.Dynamic.global.navigator.language.asInstanceOf[String])
      scope.isNegative = scope.value.startsWith("-")
      scope.unit = unit(attrs)
      scope.`type` = `type`(attrs)
      if (scope.`type` == "operation" && !scope.isNegative)
        scope.value = "+" + scope.value
      val direction = XRPFormatter.currencyDirection(js.Dynamic.global.navigator.language.asInstanceOf[String])
      if (direction == "rtl" && scope.`type` == "operation")
        scope.value = scope.value.substring(1) + scope.value.substring(0, 1)
      scope.value = scope.value + " "
      scope.style = js.Dictionary[String](
        "display" -> "inline-block",
        "direction" -> direction,
        "opacity" -> (if (scope.rawValue.isEmpty) "0" else "1")
      )
    })
  }

  override def isolateScope: Dictionary[String] = js.Dictionary(
    "rawValue" -> "=value"
  )

  override val restrict: String = "E"

  private def unit(attrs: Attributes): String = {
    sessionService.currentSession.get.chain.symbol
  }

  private def `type`(attributes: Attributes): String = {
    Option(attributes("type").toString).getOrElse("balance")
  }

}

object AmountLabel {
  def init(module: RichModule) = {
    module.directiveOf[AmountLabel]("amount")
  }

  @js.native
  trait AmountLabelScope extends Scope {
    var rawValue: String = js.native
    var value: String = js.native
    var `type`: String = js.native
    var unit: String = js.native
    var isNegative: Boolean = js.native
    var style: js.Dictionary[String] = js.native
    var hide: Boolean = js.native
  }
}