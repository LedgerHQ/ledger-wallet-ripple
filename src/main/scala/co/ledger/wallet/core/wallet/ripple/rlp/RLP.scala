package co.ledger.wallet.core.wallet.ripple.rlp

import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.core.utils.logs.Logger

/**
  *
  * RLP
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 27/06/2016.
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
object RLP extends RlpEncoder {

}

/**
  * Here until we have a test framework
  */
object RLPTests {

  def testEncodeEmptyString(): Unit = {
    val name = "testEncodeEmptyString"
    val test = ""
    val expected = "80"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult) == expected) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeShortString(): Unit = {
    val name = "testEncodeShortString"
    val test = "dog"
    val expected = "83646f67"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeSingleCharacter(): Unit = {
    val name = "testEncodeSingleCharacter"
    val test = "d"
    val expected = "64"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeLongString(): Unit = {
    val name = "testEncodeLongString"
    val test = "Lorem ipsum dolor sit amet, consectetur adipisicing elit"
    val expected = "b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeZero(): Unit = {
    val name = "testEncodeZero"
    val test = 0
    val expected = "80"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeSmallInteger(): Unit = {
    val name = "testEncodeSmallInteger"
    val test = 15
    val expected = "0f"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeMediumInteger(): Unit = {
    val name = "testEncodeMediumInteger"
    val test = 1000
    val expected = "8203e8"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeBigInteger(): Unit = {
    val name = "testEncodeBigInteger"
    val test = BigInt("100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", 16)
    val expected = "a0100102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeEmptyList(): Unit = {
    val name = "testEncodeEmptyList"
    val test = List()
    val expected = "c0"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeShortStringList(): Unit = {
    val name = "testEncodeShortStringList"
    val test = List("cat", "dog")
    val expected = "c88363617483646f67"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeLongStringList(): Unit = {
    val name = "testEncodeLongStringList"
    val test = List("cat", "Lorem ipsum dolor sit amet, consectetur adipisicing elit")
    val expected = "f83e83636174b8384c6f72656d20697073756d20646f6c6f722073697420616d65742c20636f6e7365637465747572206164697069736963696e6720656c6974"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeMultiList(): Unit = {
    val name = "testEncodeMultiList"
    val test = List(1, List("cat"), "dog", List(2))
    val expected = "cc01c48363617483646f67c102"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeMultiList2(): Unit = {
    val name = "testEncodeMultiList2"
    val test = List(List("cat", "dog"), List(1, 2), List())
    val expected = "cdc88363617483646f67c20102c0"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeEmptyListOfList(): Unit = {
    val name = "testEncodeEmptyListOfList"
    val test = List(List(List(), List()), List())
    val expected = "c4c2c0c0c0"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def testEncodeRepOfTwoListOfList(): Unit = {
    val name = "testEncodeRepOfTwoListOfList"
    val test = List( List(), List(List()), List( List(), List(List())))
    val expected = "c7c0c1c0c3c0c1c0"
    val encoderesult = RLP.encode(test)
    if (HexUtils.encodeHex(encoderesult).compareToIgnoreCase(expected) == 0) {
      Logger.i(s"Pass $name")
    } else {
      Logger.e(s"Test failed $name: got [${HexUtils.encodeHex(encoderesult)}] expected [$expected]")
    }
  }

  def test(): Unit = {
    testEncodeEmptyString()
    testEncodeShortString()
    testEncodeSingleCharacter()
    testEncodeLongString()
    testEncodeZero()
    testEncodeSmallInteger()
    testEncodeMediumInteger()
    testEncodeBigInteger()
    testEncodeEmptyList()
    testEncodeShortStringList()
    testEncodeLongStringList()
    testEncodeMultiList()
    testEncodeMultiList2()
    testEncodeEmptyListOfList()
    testEncodeRepOfTwoListOfList()
  }
}