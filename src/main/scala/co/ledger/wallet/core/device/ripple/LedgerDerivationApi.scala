package co.ledger.wallet.core.device.ripple

import java.nio.charset.Charset

import co.ledger.wallet.core.device.ripple.LedgerDerivationApi.PublicAddressResult
import co.ledger.wallet.core.utils.{BytesWriter, DerivationPath, HexUtils}
import co.ledger.wallet.core.wallet.ripple.RippleAccount

import scala.concurrent.{Future, Promise}

/**
  *
  * LedgerDerivationApi
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
trait LedgerDerivationApi extends LedgerCommonApiInterface {

  def derivePublicAddress(path: DerivationPath, askConfirmation: Boolean = false): Future[PublicAddressResult] = {
    // E0 02 00|01
    val apduPath = new BytesWriter().writeDerivationPath(path)
    sendApdu(0xE0, 0x02, if (askConfirmation) 0x01 else 0x00, 0x00, apduPath.toByteArray, 0x00) map {(result) =>
      matchErrorsAndThrow(result)
      val data = result.data
      val publicKey = data.readNextBytes(data.readNextByte())
      val address = data.readNextBytes(data.readNextByte())
      PublicAddressResult(publicKey, RippleAccount("0x" + new String(address, Charset.forName("US-ASCII"))))
    }
  }

}

object LedgerDerivationApi {

  case class PublicAddressResult(publicKey: Array[Byte], account: RippleAccount)

}