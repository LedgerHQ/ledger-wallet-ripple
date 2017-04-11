package co.ledger.wallet.core.wallet.ripple.api

import java.util.Date

import co.ledger.wallet.core.concurrent.{AbstractAsyncCursor, AsyncCursor}
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.utils.logs.Logger
import co.ledger.wallet.core.wallet.ripple.Wallet.NewOperationEvent
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.api.AbstractApiAccountClient.{AccountSavedState, AccountSavedStateBatch, SynchronizationStatus}
import co.ledger.wallet.core.wallet.ripple.database.{AccountRow, DatabaseBackedAccountClient}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
/**
  *
  * AbstractApiAccountClient
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
abstract class AbstractApiAccountClient(override val wallet
                                          : AbstractApiWalletClient,
                                        private val accountRow: AccountRow)
  extends Account
    with DatabaseBackedAccountClient {

  implicit val ec: ExecutionContext = wallet.ec

  override def index: Int = accountRow.index
  override def rippleAccount(): Future[RippleAccount] = Future.successful(RippleAccount(accountRow.rippleAccount))
  override def rippleAccountDerivationPath(): Future[DerivationPath] = Future.successful(DerivationPath(s"44'/${wallet.bip44CoinType}'${wallet.coinPathPrefix}/$index'/0"))

  override def synchronize(): Future[Unit] = wallet.synchronize()

  private[api] def synchronize(syncToken: String): Future[Unit] = {
    val startDate = new Date()
    load() flatMap {(state) =>
      Logger.i(s"Account #$index start synchronization")
      initSavedState(state)
      def synchronizeUntilEmpty(): Future[Unit] = {
        val block = Option(state.batches).flatMap(_.headOption)
          .map(_.blockHash).flatMap(Option(_))
        wallet.transactionRestClient.transactions(syncToken,
          Array(accountRow.rippleAccount), block) flatMap {(result) =>
          // Find highest block
          var highestBlock = result.transactions.headOption.flatMap(_.block)
          for (tx <- result.transactions) {
            if ((tx.block.isDefined && highestBlock.isEmpty) ||
                (tx.block.isDefined && tx.block.get.height > highestBlock
                  .get.height)) {
              highestBlock = tx.block
            }
          }
          if (highestBlock.isDefined) {
            val batch = state.batches.head
            batch.blockHash = highestBlock.get.hash
            batch.blockHeight = highestBlock.get.height
          }
          wallet.putTransactions(result.transactions) flatMap { _ =>
            val operations = createOperations(result.transactions)
            wallet.putOperations(operations) map {(_) =>
              operations foreach {(op) =>
                wallet.eventEmitter.emit(NewOperationEvent(this, op))
              }
              ()
            }
          } flatMap {_ =>
            if (result.isTruncated) {
              synchronizeUntilEmpty()
            } else {
              Future.successful()
            }
          }
        }
      }
      synchronizeUntilEmpty().flatMap({(_) =>
        wallet.transactionRestClient.getAccountBalance(accountRow.rippleAccount).flatMap {(ethers) =>
          _balanceCache = Option(ethers)
          updateAccountBalance(ethers)
        }
      }).flatMap({(_) =>
        val endDate = new Date()
        Logger.i(s"Account #$index synchronization end in ${(endDate.getTime - startDate.getTime) / 1000}s")
        state.lastSynchronizationDate = endDate.getTime
        state.lastSynchronizationStatus = SynchronizationStatus.Success
        save(state)
      }) recoverWith {
        case all: Throwable =>
          val endDate = new Date()
          Logger.e(s"Account #$index synchronization failed in ${(endDate.getTime - startDate.getTime) / 1000}s - ${all.getMessage}")
          state.lastSynchronizationDate = endDate.getTime
          state.lastSynchronizationStatus = SynchronizationStatus.Failure
          save(state)
      }
    }
  }

  def synchronizationBlockHash(): Future[Option[String]] = {
    load() map {(state) =>
      state.batches.lastOption.map(_.blockHash)
    }
  }

  def setSynchronizationBlock(block: Block): Future[Unit] = {
    load() flatMap {(state) =>
      for (batch <- state.batches) {
        batch.blockHash = block.hash
        batch.blockHeight = block.height
      }
      save(state)
    }
  }

  def putTransaction(tx: Transaction): Unit = {
    createOperations(Array(tx)) foreach {(operation) =>
      wallet.putTransactions(Array(operation.transaction)) onComplete {case all =>
        wallet.putOperations(Array(operation)) onComplete {case all =>
          wallet.eventEmitter.emit(NewOperationEvent(this, operation))
        }
      }
    }
  }

  private def createOperations(transactions: Array[Transaction]): Array[Operation] = {
    val result = new ArrayBuffer[Operation]()
    transactions foreach {(tx) =>
      _balanceCache = None
      if (tx.to == accountRow.rippleAccount) {
        // Receive
        result += new Operation {
          override def `type`: String = Operation.ReceiveType

          override def account: Account = AbstractApiAccountClient.this

          override def transaction: Transaction = tx
        }
      }
      if (tx.from == accountRow.rippleAccount) {
        // Send
        result += new Operation {
          override def `type`: String = Operation.SendType

          override def account: Account = AbstractApiAccountClient.this

          override def transaction: Transaction = tx
        }
      }
    }
    result.toArray
  }

  private def initSavedState(savedState: AccountSavedState): Unit = {
    if (savedState.batches.isEmpty) {
      savedState.batchSize = AbstractApiAccountClient.DefaultBatchSize
      savedState.index = index
      savedState.lastSynchronizationStatus = SynchronizationStatus.None
      val batch = new AccountSavedStateBatch() {
        override var index: Int = 0
        override var blockHash: String = null
        override var blockHeight: Long = 0
      }
      savedState.batches = Array(batch)
    }
  }

  protected def load(): Future[AbstractApiAccountClient.AccountSavedState]
  protected def save(state: AbstractApiAccountClient.AccountSavedState): Future[Unit]

  override def operations(limit: Int, batchSize: Int = Wallet.DefaultOperationsBatchSize): Future[AsyncCursor[Operation]] = {
    countOperations() map {(c) =>
      new AbstractAsyncCursor[Operation](ec, batchSize) {
        override protected def performQuery(from: Int, to: Int): Future[Array[Operation]] = queryOperation(from, to)

        override val count: Int = if (limit == -1 || limit > c) c.toInt else limit

        override def requery(): Future[AsyncCursor[Operation]] = operations(limit, batchSize)
      }
    }
  }

  override def balance(): Future[XRP] = _balanceCache.map((b) => Future.successful(b)) getOrElse {
    queryAccountBalance()
  } andThen {
    case Success(balance) =>
      _balanceCache = Option(balance)
    case Failure(ex) => // Do nothing
  }
  private var _balanceCache: Option[XRP] = None

  def transactionNonce(): Future[BigInt] = wallet.transactionRestClient.getAccountNonce(accountRow.rippleAccount)

  override def isSynchronizing(): Future[Boolean] = wallet.isSynchronizing()

  val keyChain = new KeyChain
}

object AbstractApiAccountClient {

  val DefaultBatchSize = 1

  object SynchronizationStatus {
    val Success = 1
    val Failure = 2
    val None = 3
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
    var blockHeight: Long
  }

}