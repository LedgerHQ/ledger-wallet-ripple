package co.ledger.wallet.core.device.ripple

import java.io.ByteArrayInputStream

import co.ledger.wallet.core.device.ripple.LedgerCommonApiInterface.CommandResult
import co.ledger.wallet.core.utils.{BytesWriter, DerivationPath}
import co.ledger.wallet.core.wallet.ripple.RippleAccount
import co.ledger.wallet.web.ripple.components.RippleSerializer

import scala.concurrent.Future

/**
  *
  * LedgerSignatureApi
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 28/06/2016.
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
trait LedgerSignatureApi extends LedgerCommonApiInterface {

  def signTransaction(from: DerivationPath,
                      preparedPayment: String): Future[Array[Byte]] = {
    val batchSize: Int = 150
    val rawDerivationPath = new BytesWriter().writeDerivationPath(from).toByteArray
    println(s"rawderiv ${rawDerivationPath.length}")
    val serialized = RippleSerializer.encode(preparedPayment)
    println(s"serialized ${serialized.length}")

    def sendChunks(i: Int): Future[CommandResult] = {
      println(s"chunk i $i")

      val offset = Math.max(i * batchSize - rawDerivationPath.length, 0)
      val length = batchSize - (if (i == 0) rawDerivationPath.length else 0)
      val chunk = (if (i == 0) rawDerivationPath else Array.empty[Byte]) ++ serialized.slice(offset, offset + length)
      println(chunk.length)
      sendApdu(0xE0, 0x04, if (i == 0) 0x00 else 0x80, 0x40, chunk, 0) flatMap {(result) =>
        matchErrorsAndThrow(result)
        if ((i + 1) * batchSize - rawDerivationPath.length < serialized.length)
          sendChunks(i + 1)
        else
          Future.successful(result)
      }
    }
    sendChunks(0) map {(result) =>
      result.data.readNextBytesUntilEnd()
    }
  }
}