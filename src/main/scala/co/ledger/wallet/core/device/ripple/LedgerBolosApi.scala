package co.ledger.wallet.core.device.ripple

import co.ledger.wallet.core.device.ripple.LedgerCommonApiInterface.{LedgerApiException, LedgerApiUnknownErrorException}
import co.ledger.wallet.core.utils.BytesReader

import scala.concurrent.Future

/**
  *
  * LedgerBolosApi
  * ledger-wallet-ripple-chrome
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
trait LedgerBolosApi extends LedgerCommonApiInterface {
  import LedgerBolosApi._

  def getFirmwareVersion(): Future[FirmwareVersion] = {
    sendApdu(0xE0, 0x01, 0x00, 0x00, 0x00, 0x00) map {(result) =>
      matchErrorsAndThrow(result)
      new FirmwareVersion(result.data)
    }
  }

  def validateTargetId(target: Array[Byte]): Future[Unit] = {
    sendApdu(0xE0, 0x04, 0x00, 0x00, 0x00, target.slice(0, 4), 0x00) map {(result) =>
      result.sw match {
        case 0x9000 =>
          ()
        case other =>
          throw LedgerApiUnknownErrorException(other)
      }
    }
  }

  def isBolosInApp(): Future[Boolean] = {
    getFirmwareVersion() flatMap {(version) =>
      validateTargetId(version.targetId)
    } map {(_) =>
      false
    } recover {
        case throwable: LedgerApiException =>
          true
        case other => throw other
      }
    }
}

object LedgerBolosApi {
  val BolosInApp = 0
  val BolosOnOnboarding = 1

  class FirmwareVersion(reader: BytesReader) {
    val targetId = reader.readNextBytes(4)
    val version = new String(reader.readNextBytes(reader.readNextByte()))
    val flags: Option[Array[Byte]] = {
      if (reader.available > 0)
        Option(reader.readNextBytes(reader.readNextByte()))
      else
        None
    }
  }

}
