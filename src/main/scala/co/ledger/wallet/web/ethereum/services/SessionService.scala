package co.ledger.wallet.web.ethereum.services

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.Service
import co.ledger.wallet.core.device.ethereum.{LedgerApi, LedgerDerivationApi, LedgerEthereumAppApi}
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ethereum.{EthereumAccount, EthereumAccountProvider, Wallet}
import co.ledger.wallet.web.ethereum.wallet.ApiWalletClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  *
  * SessionService
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
class SessionService extends Service {

  def startNewSessions(api: LedgerApi, chain: SessionService.EthereumChainIdentifier): Future[Unit] = {
    api.walletIdentifier() flatMap {(walletIdentifier) =>
      api.walletMetaPassword() flatMap {(password) =>
        api.getAppConfiguration() flatMap {(appConfiguration) =>
          val provider = new EthereumAccountProvider {
            override def getEthereumAccount(path: DerivationPath): Future[EthereumAccount] = {
              api.derivePublicAddress(path).map(_.account)
            }
          }
          val session = new Session(walletIdentifier, password, provider, chain, appConfiguration)
          _currentSession = Some(session)
          Future.successful()
        }
      }
    }
  }

  def stopCurrentSessions(): Future[Unit] = {
    println("Stop current session")
    if (_currentSession.isDefined) {
      _currentSession.get.wallet.stop()
    }
    _currentSession = None
    Future.successful()
  }

  def currentSession = _currentSession
  private var _currentSession: Option[Session] = None

  class Session(val name: String,
                val password: String,
                provider: EthereumAccountProvider,
                val chain: SessionService.EthereumChainIdentifier,
                val dongleAppVersion: LedgerEthereumAppApi.AppConfiguration) {
    val wallet: Wallet = new ApiWalletClient(name, Option(password), provider, chain)

    val sessionPreferences = scala.collection.mutable.Map[String, Any]()
  }

  SessionService.setInstance(this)
}

object SessionService {

  def instance = _instance
  private def setInstance(service: SessionService) = _instance = service
  private var _instance: SessionService = null
  def init(module: RichModule) = module.serviceOf[SessionService]("sessionService")

  sealed abstract class EthereumChainIdentifier(val id: String,
                                                val coinType: String,
                                                val pathPrefix: String,
                                                val explorerBaseUrl: String,
                                                val symbol: String)
  case class EthereumClassicChain() extends EthereumChainIdentifier(
    "ethc",
    "60",
    "/160720'",
    "https://gastracker.io/",
    "ETC"
  )
  case class EthereumChain() extends EthereumChainIdentifier(
    "eth",
    "60",
    "",
    "http://etherscan.io/",
    "ETH"
  )

}