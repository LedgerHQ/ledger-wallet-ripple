package co.ledger.wallet.web.ripple.wallet

import java.net.URI
import java.util.Date

import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.core.net.{HttpClient, WebSocketFactory}
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.api.{AbstractApiAccountClient, AbstractApiWalletClient, AbstractBlockRestClient, AbstractTransactionRestClient}
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.core.net.{JQHttpClient, JsWebSocketFactory}
import co.ledger.wallet.web.ripple.services.SessionService

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  *
  * ApiWalletClient
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
class ApiWalletClient(name: String,
                      override protected val password: Option[String],
                      provider: RippleAccountProvider,
                      chain: SessionService.RippleChainIdentifier) extends
  AbstractApiWalletClient(s"${chain.id}_${name}_${chain.coinType}_${chain
    .pathPrefix}", chain.coinType, chain.pathPrefix) with
    IndexedDBBackedWalletClient {

  override implicit val ec: ExecutionContext = scala.concurrent
    .ExecutionContext.Implicits.global

  override protected def newAccountClient(accountRow: AccountRow):
  AbstractApiAccountClient = {
    new ApiAccountClient(this, password, accountRow)
  }

  override def rippleAccountProvider: RippleAccountProvider = provider

  protected def http: HttpClient = chain match {
    case SessionService.RippleChain() =>
      JQHttpClient.xrpInstance
  }

  override val blockRestClient = new AbstractBlockRestClient(http) {
    override def stringToDate(string: String): Date = ApiWalletClient
      .this.stringToDate(string)
  }

  override val transactionRestClient = new AbstractTransactionRestClient(http, blockRestClient) {
    override def stringToDate(string: String): Date = ApiWalletClient.this.stringToDate(string)
  }

  override val eventEmitter: EventEmitter = new JsEventEmitter

  def stringToDate(string: String): Date = new Date(new js.Date(string).getTime().toLong)

  override def websocketFactory: WebSocketFactory = new JsWebSocketFactory(new URI(s"wss://api.ledgerwallet.com/blockchain/v2/eth")) //todo TEMP

  def secret: String = {???}

}
