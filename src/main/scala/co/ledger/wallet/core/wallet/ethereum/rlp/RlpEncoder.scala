package co.ledger.wallet.core.wallet.ethereum.rlp

import java.io.ByteArrayOutputStream

import co.ledger.wallet.core.utils.BytesWriter
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream

import scala.annotation.tailrec

/**
  *
  * RlpWriter
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 24/06/2016.
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
trait RlpEncoder {

  def encode(data: Any): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    performEncode(data, output)
    output.toByteArray
  }

  private def performEncode(data: Any, output: ByteArrayOutputStream): Unit = {
    data match {
      case string: String => encodeString(toByteArray(string), output)
      case long: Long => encodeString(toByteArray(long), output)
      case int: Int => encodeString(toByteArray(int), output)
      case short: Short => encodeString(toByteArray(short), output)
      case byte: Byte => encodeString(toByteArray(byte), output)
      case list: List[Any] => encoreSeq(list.toArray, output)
      case list: Seq[Any] => encoreSeq(list.toArray, output)
      case list: Array[Any] => encoreSeq(list, output)
      case notFound => throw new Exception(s"Invalid type $notFound $data")
    }
  }

  private def encoreSeq(list: Array[Any], output: ByteArrayOutputStream): Unit = {
    encodeLength(list.length, 0xC0, output)
    for (item <- list) {
      performEncode(item, output)
    }
  }

  private def encodeString(bytes: Array[Byte], output: ByteArrayOutputStream): Unit = {
    if (bytes.length == 1 && bytes(0) < 0x7F) {
      output.write(bytes(0))
    } else {
      encodeLength(bytes.length, 0x80, output)
      output.write(bytes)
    }
  }

  private def encodeLength(length: Int, offset: Int, outputStream: ByteArrayOutputStream): Unit = {
    if (length < 56) {
      outputStream.write(length + offset)
    } else  {
      val binary = toBinary(length)
      outputStream.write(binary.length + offset + 55)
      outputStream.write(binary)
    }
  }

  private def toBinary(int: Int): Array[Byte] = {
    if (int == 0)
      Array.empty[Byte]
    else
      toBinary(int / 256) ++ Array[Byte]((int % 256).toByte)
  }

  private def toByteArray(string: String): Array[Byte] = string.getBytes
  private def toByteArray(int: Int): Array[Byte] = dropNullBytes(Array[Byte](
    (int >> 24 & 0xFF).toByte,
    (int >> 16 & 0xFF).toByte,
    (int >> 8 & 0xFF).toByte,
    (int & 0xFF).toByte
  ))

  private def toByteArray(short: Short): Array[Byte] = dropNullBytes(Array[Byte](
    (short >> 8 & 0xFF).toByte,
    (short & 0xFF).toByte
  ))

  private def toByteArray(long: Long): Array[Byte] = dropNullBytes(Array[Byte](
    (long >> 56 & 0xFF).toByte,
    (long >> 48 & 0xFF).toByte,
    (long >> 40 & 0xFF).toByte,
    (long >> 32 & 0xFF).toByte,
    (long >> 24 & 0xFF).toByte,
    (long >> 16 & 0xFF).toByte,
    (long >> 8 & 0xFF).toByte,
    (long & 0xFF).toByte
  ))

  private def dropNullBytes(bytes: Array[Byte]): Array[Byte] = {
    val result = bytes.dropWhile(_ == 0x00)
    if (result.isEmpty) {
      Array(0x00.toByte)
    } else {
      result
    }
  }

  private def toByteArray(byte: Byte): Array[Byte] = Array(byte)

}
