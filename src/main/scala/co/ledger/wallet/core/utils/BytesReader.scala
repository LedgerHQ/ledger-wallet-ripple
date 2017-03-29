package co.ledger.wallet.core.utils

/**
  *
  * BytesReader
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

import java.math.BigInteger
import java.nio.charset.Charset

class BytesReader(val bytes: Array[Byte], private val startOffset: Int, val length: Int) {

  protected[this] var _offset = startOffset

  def this(bytes: Array[Byte]) {
    this(bytes, 0, bytes.length)
  }

  def read(length: Int): Array[Byte]  = {
    val offset = _offset
    seek(length)
    bytes.slice(offset, _offset)
  }

  def seek(length: Int): Unit = {
    if (length > available) {
      throw new IndexOutOfBoundsException(s"Invalid length ($length) is greater than available byte to read ($available)")
    }
    val offset = _offset
    _offset = offset + (if (length >= 0) length else available)
  }

  def readString(length: Int, charset: Charset = Charset.defaultCharset()): String = {
    new String(read(length), charset)
  }

  def readBigInteger(length: Int, signum: Int = 1): BigInteger = {
    new BigInteger(1, read(length))
  }
  def readNextShort(): Short = readBigInteger(2).shortValue()
  def readNextInt(): Int = readBigInteger(4).intValue()
  def readNextLong(): Long = readBigInteger(8).longValue()

  def readLeBigInteger(length: Int, signum: Int = 1): BigInteger = {
    new BigInteger(1, read(length).reverse)
  }
  def readNextLeShort(): Int = readLeBigInteger(2).intValue()
  def readNextLeInt(): Int = readLeBigInteger(4).intValue()
  def readNextLeLong(): Long = readLeBigInteger(8).longValue()

  def readNextBytes(length: Int): Array[Byte] = {
    val bytes = new Array[Byte](length)
    for (i <- bytes.indices)
      bytes(i) = readNextByte()
    bytes
  }

  def readNextBytesUntilEnd(): Array[Byte] = readNextBytes(length - _offset)

  def readNextByte(): Byte = read(1)(0)

  /*
  def readNextVarInt(): VarInt = {
    val varInt = new VarInt(bytes, _offset)
    seek(varInt.getOriginalSizeInBytes)
    varInt
  }
  */

  def slice(offset: Int, size: Int): BytesReader = new BytesReader(bytes, offset, size)

  def available: Int = bytes.length - _offset

  def apply(index: Int): Byte = bytes(index)
}