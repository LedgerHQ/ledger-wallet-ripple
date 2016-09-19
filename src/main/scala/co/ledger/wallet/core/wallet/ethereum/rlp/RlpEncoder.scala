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
      case bigInt: BigInt => encodeString(toByteArray(bigInt), output)
      case long: Long => encodeString(toByteArray(long), output)
      case int: Int => encodeString(toByteArray(int), output)
      case short: Short => encodeString(toByteArray(short), output)
      case byte: Byte => encodeString(toByteArray(byte), output)
      case bytes: Array[Byte] => encodeString(bytes, output)
      case list: List[Any] => encodeList(list.toArray, output)
      case notFound => throw new Exception(s"Invalid type $notFound $data")
    }
  }

  private def encodeList(list: Array[Any], output: ByteArrayOutputStream): Unit = {

    val childOutput = new ByteArrayOutputStream()
    for (item <- list) {
      performEncode(item, childOutput)
    }
    val children = childOutput.toByteArray
    encodeLength(children.length, 0xC0, output)
    output.write(children)
  }

  private def encodeString(bytes: Array[Byte], output: ByteArrayOutputStream): Unit = {
    if (bytes.length == 1 && bytes(0) <= 0x7F) {
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
  private def toByteArray(int: Int): Array[Byte] = toByteArray(BigInt(int))
  private def toByteArray(short: Short): Array[Byte] = toByteArray(BigInt(short))
  private def toByteArray(long: Long): Array[Byte] = toByteArray(BigInt(long))
  private def toByteArray(bigInt: BigInt): Array[Byte] = dropNullBytes(bigInt.toByteArray)

  private def dropNullBytes(bytes: Array[Byte]): Array[Byte] = bytes.dropWhile(_ == 0x00)


  private def toByteArray(byte: Byte): Array[Byte] = Array(byte)

}
