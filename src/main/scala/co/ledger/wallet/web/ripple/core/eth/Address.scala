package co.ledger.wallet.web.ripple.core.eth

import co.ledger.wallet.core.utils.HexUtils

/**
  *
  * Address
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 06/05/2016.
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
class Address(bytes: Array[Byte]) {

  def toIBAN = {
    val bban = toBigInt.toString(36).toUpperCase
    val checksumed = (bban + "XE00").map(_.toString).map {(c) =>
      if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z')
        (c.charAt(0) - 'A' + 10).toString
      else
        c
    }.mkString("")
    val checksum = ("0" + (98 - Address.mod9710(checksumed)).toString).takeRight(2)
    s"XE$checksum$bban"
  }

  def toBigInt = BigInt(bytes)
  override def toString: String = "0x" + HexUtils.bytesToHex(bytes)


}

object Address {

  def apply(str: String): Address = {
    if (str.startsWith("iban:") || str.startsWith("XE"))
      fromIban(str)
    else
      fromHex(str)
  }

  def fromIban(iban: String): Address = {
    if (!iban.startsWith("XE")) {
      throw new Exception(s"[$iban] is not a valid ICAP")
    }
    val checksum = iban.substring(2, 4).toInt
    val bban = BigInt(iban.substring(4), 36)
    val checksumed = (iban.substring(4) + iban.substring(0, 4)).map(_.toString).map {(c) =>
      if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z')
        (c.charAt(0) - 'A' + 10).toString
      else
        c
    }.mkString("")
    if (mod9710(checksumed) != 1) {
      throw new Exception(s"[$iban] is not a valid ICAP (invalid checksum)")
    }
    new Address(bban.toByteArray)
  }

  def fromHex(hex: String): Address = {
    if (hex.startsWith("0x"))
      new Address(HexUtils.decodeHex(hex.substring(2)))
    else
      new Address(HexUtils.decodeHex(hex))
  }

  private def mod9710(value: String) = value.foldLeft(0)((r, c) => (r * 10 + (c - '0')) % 97)
}

