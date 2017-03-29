package co.ledger.wallet.core.wallet.ripple

import java.io.StringWriter

import co.ledger.wallet.core.crypto.Keccak
import co.ledger.wallet.core.utils.HexUtils

/**
  *
  * RippleAccount
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 14/06/2016.
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
/***
  * Wrapper for "Address"
  */
class RippleAccount(value: BigInt) {

  def toIban: String = {
    val bban = value.toString(36).toUpperCase
    val checksumed = (bban + "XE00").map(_.toString).map {(c) =>
      if (c.charAt(0) >= 'A' && c.charAt(0) <= 'Z')
        (c.charAt(0) - 'A' + 10).toString
      else
        c
    }.mkString("")
    val checksum = ("0" + (98 - RippleAccount.mod9710(checksumed)).toString).takeRight(2)
    s"XE$checksum$bban"
  }

  def toChecksumString: String = {
    val out = new StringWriter()
    out.append("0x")
    val address = toString.substring(2)
    val checksum = HexUtils.encodeHex(Keccak.hash256(address.getBytes))
    for (index <- address.indices) {
      if (Integer.parseInt(checksum(index).toString, 16) >= 8) {
        out.append(address(index).toUpper)
      } else {
        out.append(address(index).toLower)
      }
    }
    out.toString
  }
  override def toString: String = "0x" + HexUtils.bytesToHex(toByteArray).toLowerCase
  def toByteArray = {
    var bytes = value.toByteArray
    if (bytes.length > 20) {
      bytes = bytes.slice(1, bytes.length)
    }
    if (bytes.length < 20) {
      bytes = bytes.reverse.padTo(20, 0.toByte).reverse
    }
    bytes
  }
  override def hashCode(): Int = value.hashCode()
  override def equals(obj: scala.Any): Boolean = value.equals(obj)
}

object RippleAccount {

  def apply(str: String): RippleAccount = {
    if (str.startsWith("iban:") || str.startsWith("XE"))
      fromIban(str)
    else
      fromHex(str)
  }

  def fromIban(iban: String): RippleAccount = {
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
    new RippleAccount(bban)
  }

  def fromHex(hex: String): RippleAccount = {
    if (hex.length != 40 && hex.length != 42)
      throw new Exception(s"[$hex] is not a valid hex ripple account address")
    if (hex.exists((c) => !c.isDigit && c.isUpper) &&
        hex.exists((c) => !c.isDigit && c.isLower && c != 'x') &&
        !isValidHexAddress(hex)) {
      throw new Exception(s"[$hex] has an invalid checksum")
    }
    if (hex.startsWith("0x"))
      new RippleAccount(BigInt(hex.substring(2), 16))
    else
      new RippleAccount(BigInt(hex, 16))
  }

  def isValidHexAddress(address: String): Boolean = {
    if (address.startsWith("0x"))
      new RippleAccount(BigInt(address.substring(2), 16)).toChecksumString == address.toString
    else
      new RippleAccount(BigInt(address, 16)).toChecksumString == ("0x" + address.toString)
  }

  private def mod9710(value: String) = value.foldLeft(0)((r, c) => (r * 10 + (c - '0')) % 97)
}