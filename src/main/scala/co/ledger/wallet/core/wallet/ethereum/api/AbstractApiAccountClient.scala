package co.ledger.wallet.core.wallet.ethereum.api

import java.util.Date

import co.ledger.wallet.core.concurrent.{AbstractAsyncCursor, AsyncCursor}
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.utils.logs.Logger
import co.ledger.wallet.core.wallet.ethereum._
import co.ledger.wallet.core.wallet.ethereum.api.AbstractApiAccountClient.{AccountSavedState, AccountSavedStateBatch, SynchronizationStatus}
import co.ledger.wallet.core.wallet.ethereum.database.{AccountRow, DatabaseBackedAccountClient}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}
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

  implicit val ec: ExecutionContext = wallet.ec

  override def index: Int = accountRow.index
  override def ethereumAccount(): Future[EthereumAccount] = Future.successful(EthereumAccount(accountRow.ethereumAccount))
  override def ethereumAccountDerivationPath(): Future[DerivationPath] = Future.successful(DerivationPath(s"44'/60'/$index'/0"))

  override def synchronize(): Future[Unit] = wallet.synchronize()

  private[api] def synchronize(syncToken: String): Future[Unit] = {
    val startDate = new Date()
    load() flatMap {(state) =>
      Logger.i(s"Account #$index start synchronization")
      initSavedState(state)
      def synchronizeUntilEmpty(): Future[Unit] = {
        val block = Option(state.batches).flatMap(_.headOption).map(_.blockHash).flatMap(Option(_))
        wallet.transactionRestClient.transactions(syncToken, Array(accountRow.ethereumAccount), block) flatMap {(result) =>
          // Find highest block
          var highestBlock = result.transactions.headOption.flatMap(_.block)
          for (tx <- result.transactions) {
            if ((tx.block.isDefined && highestBlock.isEmpty) ||
                (tx.block.isDefined && tx.block.get.height > highestBlock.get.height)) {
              highestBlock = tx.block
            }
          }
          if (highestBlock.isDefined) {
            val batch = state.batches.head
            batch.blockHash = highestBlock.get.hash
            batch.blockHeight = highestBlock.get.height
          }
          wallet.putTransactions(result.transactions) flatMap { _ =>
            wallet.putOperations(createOperations(result.transactions))
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

  private def createOperations(transactions: Array[Transaction]): Array[Operation] = {
    val result = new ArrayBuffer[Operation]()
    transactions foreach {(tx) =>
      _balanceCache = None
      if (tx.to == accountRow.ethereumAccount) {
        // Receive
        result += new Operation {
          override def `type`: String = Operation.ReceiveType

          override def account: Account = AbstractApiAccountClient.this

          override def transaction: Transaction = tx
        }
      }
      if (tx.from == accountRow.ethereumAccount) {
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

  override def balance(): Future[Ether] = _balanceCache.map((b) => Future.successful(b)) getOrElse {
    operations() flatMap {(cursor) =>
      var sum = BigInt(0)
      def consume(index: Int = 0): Future[BigInt] = {
        cursor.loadChunk(index) flatMap {(chunk) =>
          chunk.foreach {(op) =>
            if (op.`type` == Operation.ReceiveType)
              sum = sum + op.transaction.value.toBigInt
            else
              sum = sum - op.transaction.value.toBigInt - (op.transaction.gasUsed.toBigInt * op.transaction.gasPrice.toBigInt)
          }
          if (index + 1 < cursor.chunkCount) {
            consume(index + 1)
          } else {
            Future.successful(sum)
          }
        }
      }
      consume() map {(v) =>
        new Ether(v)
      }
    }
  } andThen {
    case Success(balance) =>
      _balanceCache = Option(balance)
    case Failure(ex) => // Do nothing
  }
  private var _balanceCache: Option[Ether] = None

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