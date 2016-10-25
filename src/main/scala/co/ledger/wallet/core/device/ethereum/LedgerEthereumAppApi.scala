package co.ledger.wallet.core.device.ethereum

import co.ledger.wallet.core.device.ethereum.LedgerEthereumAppApi.AppConfiguration
import co.ledger.wallet.core.utils.BytesReader

import scala.concurrent.Future

/**
  *
  * LedgerEthereumAppApi
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 25/10/2016.
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
trait LedgerEthereumAppApi extends LedgerCommonApiInterface {

  def getAppConfiguration(): Future[LedgerEthereumAppApi.AppConfiguration] = {
    sendApdu(0xE0, 0x06, 0x00, 0x00, 0x00, 0x04) map {(data) =>
      new AppConfiguration(data.data)
    } recover {
      case all =>
        new AppConfiguration(new BytesReader(Array[Byte](0x00, 0x01, 0x00, 0x00)))
    }
  }

}

object LedgerEthereumAppApi {

  class AppConfiguration(reader: BytesReader) {
    val flags = reader.readNextByte()
    val major = reader.readNextByte()
    val minor = reader.readNextByte()
    val patch = reader.readNextByte()

    val version = s"$major.$minor.$patch"

    def isArbitraryDataSignatureEnabled = (flags & 0x01) == 0x01

    def compare(version: String): Int = {
      import scala.util.matching.Regex
      val pattern = "([0-9]+)\\.([0-9]+)\\.([0-9]+)".r
      val pattern(major, minor, patch) = version
      (this.major << 16 + this.minor << 8 + this.patch) - (major.toInt << 16 + minor.toInt << 8 + patch.toInt)
    }

    def <(version: String) = compare(version) < 0
    def <=(version: String) = compare(version) <= 0
    def ==(version: String) = compare(version) == 0
    def >=(version: String) = compare(version) >= 0
    def >(version: String) = compare(version) > 0
  }

}