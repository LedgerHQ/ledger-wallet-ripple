package co.ledger.wallet.web.ripple.filters

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.QService
import org.widok.moment.Moment

import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.js
import scala.scalajs.js.timers

/**
  *
  * DateFormat
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 12/07/2016.
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
object DateFormatFilter {

  def init(module: RichModule) = {
    module.filter("date", {($translate: js.Dynamic, $q: QService) =>
      ({(date: js.Date, format: String) =>
        val i18nKey = s"dates.$format"
        val i18nFormat = $translate.instant(i18nKey).asInstanceOf[String]
        Moment(date.getTime).format(if (i18nKey == i18nFormat) format else i18nFormat)
      }):js.Function
    })
  }

}
