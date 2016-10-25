package co.ledger.wallet.core.device.ethereum

import java.util.UUID

import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.DeviceManager.ConnectivityTypes
import co.ledger.wallet.core.utils.{DerivationPath, HexUtils}
import co.ledger.wallet.core.utils.DerivationPath.Root

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * LedgerApi
  * ledger-wallet-ethereum-chrome
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
class LedgerApi(override val device: Device)
  extends LedgerCommonApiInterface
  with LedgerDerivationApi
  with LedgerBolosApi
  with LedgerSignatureApi
  with LedgerEthereumAppApi {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  def walletIdentifier(): Future[String] = {
    derivePublicAddress(DerivationPath("44'/60'/0'/0'")).map {(result) =>
      result.account.toString
    }
  }
  def walletMetaPassword(): Future[String] = {
    if (_walletIdentifier.isEmpty || (_walletIdentifier.get.isCompleted && _walletIdentifier.get.value.get.isFailure)) {
      _walletIdentifier = Some(derivePublicAddress(DerivationPath("44'/60'/14'/5'/16")).map {(result) =>
        HexUtils.encodeHex(result.publicKey)
      })
    }
    _walletIdentifier.get
  }

  val uuid = UUID.randomUUID()
  private var _walletIdentifier: Option[Future[String]] = None
}

object LedgerApi {

  def apply(device: Device): LedgerApi = {
    val lastApi = _lastApi.filter(device.uuid == _.device.uuid)
    lastApi.getOrElse {
      val api = device.connectivityType match {
        case others => new LedgerApi(device)
      }
      _lastApi = Some(api)
      api
    }
  }

  def apply(uuid: UUID): Option[LedgerApi] = _lastApi filter(_.uuid == uuid)

  private var _lastApi: Option[LedgerApi] = None
}
