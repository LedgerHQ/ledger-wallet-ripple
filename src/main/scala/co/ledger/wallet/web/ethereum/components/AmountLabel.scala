package co.ledger.wallet.web.ethereum.components

import biz.enef.angulate.{Directive, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{Attributes, JQLite}
import co.ledger.wallet.core.wallet.ethereum.Ether
import co.ledger.wallet.web.ethereum.components.AmountLabel.AmountLabelScope
import co.ledger.wallet.web.ethereum.core.utils.EtherFormatter

import scala.scalajs.js

/**
  *
  * AmountLabel
  * ledger-wallet-ethereum-chrome
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
class AmountLabel($locale: js.Dynamic, $translate: js.Dynamic) extends Directive {
  override def templateUrl: String = "templates/components/amount.html"

  override type ScopeType = AmountLabelScope

  override def postLink(scope: AmountLabelScope, element: JQLite, attrs: Attributes): Unit = {
    scope.$watch(attrs("value"), {(value: String) =>
      scope.value = EtherFormatter.format(Ether(if (value.isEmpty) "1000000000000000000" else value), unit(attrs), "en-US")
      scope.isNegative = scope.value.startsWith("-")
      scope.unit = unit(attrs)
      scope.`type` = `type`(attrs)
      if (scope.`type` == "operation" && !scope.isNegative)
        scope.value = "+" + scope.value
      val direction = EtherFormatter.currencyDirection
      scope.style = js.Dictionary[String](
        "display" -> "inline-block",
        "direction" -> direction,
        "opacity" -> (if (value.isEmpty) "0" else "1")
      )
    })
  }

  private def unit(attrs: Attributes): String = {
    "ETH"
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
    var value: String = js.native
    var `type`: String = js.native
    var unit: String = js.native
    var isNegative: Boolean = js.native
    var style: js.Dictionary[String] = js.native
    var hide: Boolean = js.native
  }
}