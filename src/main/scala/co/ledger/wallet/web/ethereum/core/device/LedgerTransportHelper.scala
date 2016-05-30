package co.ledger.wallet.web.ethereum.core.device

import java.io.ByteArrayOutputStream

/**
  *
  * LedgerTransportHelper
  * Default (Template) Project
  *
  * Created by Pierre Pollastri on 27/05/2016.
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
object LedgerTransportHelper {

  private val TAG_APDU = 0x05

  def wrapCommandAPDU(channel: Int, command: Array[Byte], packetSize: Int): Array[Byte] = {
    val output = new ByteArrayOutputStream()
    if (packetSize < 3) {
      throw new Exception("Can't handle Ledger framing with less than 3 bytes for the report")
    }
    var sequenceIdx = 0
    var offset = 0
    output.write(channel >> 8)
    output.write(channel)
    output.write(TAG_APDU)
    output.write(sequenceIdx >> 8)
    output.write(sequenceIdx)
    sequenceIdx += 1
    output.write(command.length >> 8)
    output.write(command.length)
    var blockSize = (if (command.length > packetSize - 7) packetSize - 7 else command.length)
    output.write(command, offset, blockSize)
    offset += blockSize
    while (offset != command.length) {
      output.write(channel >> 8)
      output.write(channel)
      output.write(TAG_APDU)
      output.write(sequenceIdx >> 8)
      output.write(sequenceIdx)
      sequenceIdx += 1
      blockSize = (if (command.length - offset > packetSize - 5) packetSize - 5 else command.length - offset)
      output.write(command, offset, blockSize)
      offset += blockSize
    }
    if ((output.size % packetSize) != 0) {
      val padding = Array.ofDim[Byte](packetSize - (output.size % packetSize))
      output.write(padding, 0, padding.length)
    }
    output.toByteArray()
  }

  def unwrapResponseAPDU(channel: Int, data: Array[Byte], packetSize: Int): Array[Byte] = {
    val response = new ByteArrayOutputStream()
    var offset = 0
    var responseLength: Int = 0
    var sequenceIdx = 0
    if ((data == null) || (data.length < 7 + 5)) {
      return null
    }
    if (data(offset) != (channel >> 8)) {
      throw new Exception("Invalid channel")
    }
    offset += 1
    if (data(offset) != (channel & 0xff)) {
      throw new Exception("Invalid channel")
    }
    offset += 1
    if (data(offset) != TAG_APDU) {
      throw new Exception("Invalid tag")
    }
    offset += 1
    if (data(offset) != 0x00) {
      throw new Exception("Invalid sequence")
    }
    offset += 1
    if (data(offset) != 0x00) {
      throw new Exception("Invalid sequence")
    }
    offset += 1
    responseLength = (data(offset) & 0xff) << 8
    offset += 1
    responseLength |= (data(offset) & 0xff)
    offset += 1
    if (data.length < 7 + responseLength) {
      return null
    }
    var blockSize = if (responseLength > packetSize - 7) packetSize - 7 else responseLength
    response.write(data, offset, blockSize)
    offset += blockSize
    while (response.size != responseLength) {
      sequenceIdx += 1
      if (offset == data.length) {
        return null
      }
      if (data(offset) != (channel >> 8)) {
        throw new Exception("Invalid channel")
      }
      offset += 1
      if (data(offset) != (channel & 0xff)) {
        throw new Exception("Invalid channel")
      }
      offset += 1
      if (data(offset) != TAG_APDU) {
        throw new Exception("Invalid tag")
      }
      offset += 1
      if (data(offset) != (sequenceIdx >> 8)) {
        throw new Exception("Invalid sequence")
      }
      offset += 1
      if (data(offset) != (sequenceIdx & 0xff)) {
        throw new Exception("Invalid sequence")
      }
      offset += 1
      blockSize = if (responseLength - response.size > packetSize - 5) packetSize - 5 else responseLength - response.size
      if (blockSize > data.length - offset) {
        return null
      }
      response.write(data, offset, blockSize)
      offset += blockSize
    }
    response.toByteArray
  }
}
