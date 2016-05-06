/**
  *
  * HexUtils
  * Ledger wallet
  *
  * Created by Pierre Pollastri on 02/10/15.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 Ledger
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
package co.ledger.wallet.core.utils

import java.util.{Arrays}

trait HexUtils {

  private val hexArray = "0123456789ABCDEF".toCharArray
  private val AID_PREFIX = "A00000061700"
  private val AID_SUFFIX = "0101"
  private val SELECT_HEADER = "00A40400"

  def encodeHex(bytes: Array[Byte]): String = {
    val hexChars = Array.ofDim[Char](bytes.length * 2)
    for (i <- 0 until bytes.length) {
      val v = bytes(i) & 0xFF
      hexChars(i * 2) = hexArray(v >>> 4)
      hexChars(i * 2 + 1) = hexArray(v & 0x0F)
    }
    new String(hexChars)
  }

  def decodeHex(hexString: String): Array[Byte] = {
    if ((hexString.length & 0x01) != 0) {
      throw new IllegalArgumentException("Odd number of characters.")
    }
    val hexChars = hexString.toUpperCase().toCharArray()
    val result = Array.ofDim[Byte](hexChars.length / 2)
    var i = 0
    while (i < hexChars.length) {
      result(i / 2) = (Arrays.binarySearch(hexArray, hexChars(i)) * 16 + Arrays.binarySearch(hexArray,
        hexChars(i + 1))).toByte
      i += 2
    }
    result
  }

  def stringToHex(s: String): Int ={
    Integer.decode(s)
  }

  def bytesToHex(bytes: Array[Byte]): String = {
    val hexChars = Array.ofDim[Char](bytes.length * 2)
    for (j <- 0 until bytes.length) {
      val v = bytes(j) & 0xFF
      hexChars(j * 2) = hexArray(v >>> 4)
      hexChars(j * 2 + 1) = hexArray(v & 0x0F)
    }
    new String(hexChars)
  }

  implicit class HexString(val str: String) {

    def decodeHex(): Array[Byte] = HexUtils.decodeHex(str)

  }

}

object HexUtils extends HexUtils