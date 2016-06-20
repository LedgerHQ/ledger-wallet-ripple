package co.ledger.wallet.web.ethereum.wallet

import java.util.Date

import co.ledger.wallet.core.wallet.ethereum.api.AbstractApiAccountClient
import co.ledger.wallet.core.wallet.ethereum.api.AbstractApiAccountClient.{AccountSavedState, AccountSavedStateBatch}
import co.ledger.wallet.core.wallet.ethereum.database.AccountRow

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
/**
  *
  * ApiAccountClient
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 15/06/2016.
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
class ApiAccountClient(apiWalletClient: ApiWalletClient, accountRow: AccountRow)
  extends AbstractApiAccountClient(apiWalletClient, accountRow) {

  override protected def load(): Future[AccountSavedState] = {
    Future.successful(new AccountSavedState {
      override var batches: Array[AccountSavedStateBatch] = Array()
      override var lastSynchronizationDate: Long = new Date().getTime
      override var lastSynchronizationStatus: Int = 1
      override var batchSize: Int = 20
      override var index: Int = accountRow.index
    })
  }

  override protected def save(state: AccountSavedState): Future[Unit] = {
    Future.successful()
  }
}
