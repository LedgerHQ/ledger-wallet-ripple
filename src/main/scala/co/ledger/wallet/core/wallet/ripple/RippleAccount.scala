package co.ledger.wallet.core.wallet.ripple

import java.io.StringWriter

import co.ledger.wallet.core.crypto.Keccak
import co.ledger.wallet.core.utils.HexUtils
import org.scalajs.dom
import org.scalajs.dom.crypto.GlobalCrypto

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
class RippleAccount(value: String) {
  val id = value
  def toByteArray = {
    val byte: Array[Byte] = value.getBytes("utf-16")
  }
  override def hashCode(): Int = value.hashCode()
  override def equals(obj: scala.Any): Boolean = value.equals(obj)

  override def toString: String = value
}

object RippleAccount {
  def apply(hex: String): RippleAccount = {
    if (!isValidHexAddress(hex))
      throw new Exception(s"[$hex] is not a valid ripple account address")
    new RippleAccount(hex)
  }

  def isValidHexAddress(address: String): Boolean = {
    true //GlobalCrypto.crypto.subtle.digest() //TODO
  }

}