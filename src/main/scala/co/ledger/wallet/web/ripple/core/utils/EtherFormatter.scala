package co.ledger.wallet.web.ripple.core.utils

import co.ledger.wallet.core.wallet.ripple.XRP

import scala.scalajs.js

/**
  *
  * EtherFormatter
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
object EtherFormatter {

  def format(ether: XRP, unit: String, locale: String): String = {
    val decimalSep =  js.JSNumberOps.enableJSNumberExtOps(1.6).self.toLocaleString(locale).asInstanceOf[String].charAt(1).toString
    val groupSep = js.JSNumberOps.enableJSNumberExtOps(1000).self.toLocaleString(locale).asInstanceOf[String].charAt(1).toString
    val dimension = unitToDimension(unit)
    val bigInt = ether.toBigInt
    var wei =
      if (bigInt < 0)
        (bigInt * -1).toString()
      else
        bigInt.toString()
    wei = ("0" * (dimension - wei.length)) + wei
    var integerPart = wei.substring(0, wei.length - dimension)
    var decimalPart = wei.substring(wei.length - dimension)
    // Grouping
    integerPart = integerPart.reverse.grouped(3).mkString(groupSep).reverse
    // Cleaning
    decimalPart = decimalPart.reverse.dropWhile(_ == '0').reverse
    (if (bigInt < 0) "-" else "") + (if (integerPart.length == 0) "0" else integerPart) + (if (decimalPart.length > 0) decimalSep + decimalPart else "")
  }

  def unitToDimension(unit: String) = Units.lift(unit).getOrElse(Units("ether"))

  def currencyDirection(locale: String) = {
    val f = js.JSNumberOps.enableJSNumberExtOps(10).self.toLocaleString(locale, js.Dictionary("style" -> "currency", "currency" -> "USD")).asInstanceOf[String]
    if (f.startsWith("$")) {
      "rtl"
    } else {
      "ltr"
    }
  }

  val Units = Map(
    "tether" -> 30,
    "gether" -> 27,
    "mether" -> 24,
    "kether" -> 21,
    "ether" -> 18,
    "finney" -> 15,
    "szabo" -> 12,
    "gwei" -> 9,
    "mwei" -> 6,
    "kwei" -> 3,
    "wei" -> 0,
    "gas" -> 0
  )
}
