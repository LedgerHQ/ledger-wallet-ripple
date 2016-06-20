package co.ledger.wallet.core.wallet.ethereum.api

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.wallet.ethereum._
import co.ledger.wallet.core.wallet.ethereum.database.{AccountRow, DatabaseBackedAccountClient}

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
/**
  *
  * AbstractApiAccountClient
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
abstract class AbstractApiAccountClient(override val wallet: AbstractApiWalletClient,
                                        private val accountRow: AccountRow)
  extends Account
    with DatabaseBackedAccountClient {

  override def index: Int = accountRow.index
  override def freshEthereumAccount(): Future[EthereumAccount] = Future.successful(EthereumAccount(accountRow.ethereumAccount))

  override def synchronize(): Future[Unit] = wallet.synchronize()

  private[api] def synchronize(syncToken: String): Future[Unit] = {
    load() flatMap {(state) =>
      println(s"Loaded ${accountRow.index}")
      def synchronizeUntilEmpty(): Future[Unit] = {
        val block = Option(state.batches).flatMap(_.headOption).map(_.blockHash)
        wallet.transactionRestClient.transactions(syncToken, Array(accountRow.ethereumAccount), block) flatMap {(result) =>
          wallet.putTransactions(result.transactions) flatMap {_ =>
            if (result.isTruncated) {
              synchronizeUntilEmpty()
            } else {
              Future.successful()
            }
          }
        }
      }
      synchronizeUntilEmpty().flatMap((_) => save(state))
    }
  }

  protected def load(): Future[AbstractApiAccountClient.AccountSavedState]
  protected def save(state: AbstractApiAccountClient.AccountSavedState): Future[Unit]

  override def operations(limit: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override def balance(): Future[Ether] = ???

  override def isSynchronizing(): Future[Boolean] = wallet.isSynchronizing()

  val keyChain = new KeyChain
}

object AbstractApiAccountClient {

  object SynchronizationStatus {
    val Success = 1
    val Failure = 2
  }

  trait AccountSavedState {
    var index: Int
    var batchSize: Int
    var lastSynchronizationDate: Long
    var lastSynchronizationStatus: Int
    var batches: Array[AccountSavedStateBatch]
  }

  trait AccountSavedStateBatch {
    var index: Int
    var blockHash: String
    var blockHeight: Int
  }

}