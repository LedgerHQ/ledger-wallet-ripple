package co.ledger.wallet.core.utils

import java.io.ByteArrayOutputStream

/**
  *
  * BytesWriter
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
class BytesWriter(length: Int) {

  def this() {
    this(-1)
  }

  def writeByteArray(array: Array[Byte]): BytesWriter = {
    for (byte <- array)
      writeByte(byte)
    this
  }

  def writeReversedByteArray(array: Array[Byte]): BytesWriter = writeByteArray(array.reverse)

  def writeByte(byte: Int): BytesWriter = {
    writeByte(byte.toByte)
  }

  def writeByte(byte: Byte): BytesWriter = {
    _buffer.write(byte)
    this
  }

  def writeInt(int: Int): BytesWriter = {
    writeByte((int >> 24 & 0xFF).toByte)
    writeByte((int >> 16 & 0xFF).toByte)
    writeByte((int >> 8 & 0xFF).toByte)
    writeByte((int & 0xFF).toByte)
  }

  def writeInt(int: Long): BytesWriter = {
    writeByte((int >> 24 & 0xFF).toByte)
    writeByte((int >> 16 & 0xFF).toByte)
    writeByte((int >> 8 & 0xFF).toByte)
    writeByte((int & 0xFF).toByte)
  }

  def writeLeInt(int: Int): BytesWriter = {
    writeByte((int & 0xFF).toByte)
    writeByte((int >> 8 & 0xFF).toByte)
    writeByte((int >> 16 & 0xFF).toByte)
    writeByte((int >> 24 & 0xFF).toByte)
  }

  def writeLeInt(int: Long): BytesWriter = {
    writeByte((int & 0xFF).toByte)
    writeByte((int >> 8 & 0xFF).toByte)
    writeByte((int >> 16 & 0xFF).toByte)
    writeByte((int >> 24 & 0xFF).toByte)
  }

  def writeLong(long: Long): BytesWriter = {
    val bytes = new Array[Byte](8)
    for (i <- 0 until 8) {
      bytes(i) = (long >> (8 - i - 1 << 3)).toByte
    }
    writeByteArray(bytes)
  }

  def writeLeLong(long: Long): BytesWriter = {
    val bytes = new Array[Byte](8)
    for (i <- 0 until 8) {
      bytes(i) = (long >> (8 - i - 1 << 3)).toByte
    }
    writeReversedByteArray(bytes)
  }

  def writeVarInt(int: Long): BytesWriter = {
    if (int < 0xfd) {
      writeByte(int.toByte)
    } else if (int <= 0xffff) {
      writeByte(0xfd)
      writeByte((int & 0xff).toByte)
      writeByte(((int >> 8) & 0xff).toByte)
    } else {
      writeByte(0xfe)
      writeByte((int & 0xff).toByte)
      writeByte(((int >> 8) & 0xff).toByte)
      writeByte(((int >> 16) & 0xff).toByte)
      writeByte(((int >> 24) & 0xff).toByte)
    }
  }

  def writeDerivationPath(path: DerivationPath): BytesWriter = {
    this.writeByte(path.length)
    for (i <- 0 to path.depth) {
      val n = path(i).get
      this.writeInt(n.childNum)
    }
    this
  }

  def toByteArray = _buffer.toByteArray

  private[this] val _buffer: ByteArrayOutputStream = {
    if (length > 0)
      new ByteArrayOutputStream(length)
    else
      new ByteArrayOutputStream()
  }

}
