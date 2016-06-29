package co.ledger.wallet.web.ethereum.wallet

import java.util.Date

import co.ledger.wallet.core.utils.logs.Logger
import co.ledger.wallet.core.wallet.ethereum.database.DatabaseBackedAccountClient
import co.ledger.wallet.core.wallet.ethereum._
import co.ledger.wallet.web.ethereum.content.{OperationModel, TransactionModel}
import org.scalajs.dom.idb.Cursor

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{ExecutionContext, Future}

/**
  *
  * IndexedDBBackedAccountClient
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 21/06/2016.
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
trait IndexedDBBackedAccountClient extends DatabaseBackedAccountClient {
  implicit val ec: ExecutionContext
  def wallet: IndexedDBBackedWalletClient

  override def transactionNonce(): Future[BigInt] = ethereumAccount() flatMap {(account) =>
    val address = account.toString
    wallet.TransactionModel.readonly().openCursor("nonce").reverse().cursor flatMap {(cursor) =>
      def findLastTransaction(): Future[Option[TransactionModel]] = {
        cursor.value match {
          case Some(transaction) =>
            if (transaction.from().get == address) {
              Future.successful(Some(transaction))
            } else {
              cursor.continue() flatMap {(_) =>
                findLastTransaction()
              }
            }
          case None =>
            Future.successful(None)
        }
      }
      findLastTransaction()
    }
  } map {(transaction) =>
    transaction.map((tx) => BigInt(tx.nonce().get, 16) + 1).getOrElse(BigInt(0))
  }

  override def queryOperation(from: Int, to: Int): Future[Array[Operation]] = {
    wallet.OperationModel.readonly().openCursor("time").reverse().cursor flatMap {(cursor) =>
      val length = to - from
      val result = new ArrayBuffer[OperationModel](length)
      def iterate(): Future[Array[OperationModel]] = {
        if (cursor.value.isEmpty || result.length >= length) {
          Future.successful(result.toArray)
        } else {
          if (cursor.value.get.accountId().get == index)
            result += cursor.value.get
          cursor.continue() flatMap {(_) =>
            iterate()
          }
        }
      }
      cursor.advance(from) flatMap {(_) =>
        iterate()
      } flatMap {(models) =>
        val result = new ArrayBuffer[Operation](length)
        def iterate(index: Int = 0): Future[Array[Operation]] = {
          if (index >= models.length) {
            Future.successful(result.toArray)
          } else {
            getOperation(models(index)) flatMap {(op) =>
              result += op
              iterate(index + 1)
            }
          }
        }
        iterate()
      }
    }
  }

  private def getOperation(model: OperationModel): Future[Operation] = {
    wallet.TransactionModel.readonly().get(model.transactionHash().get).items map {(txs) =>
      txs.head
    } flatMap {(tx) =>
      tx.blockHash() map {(blockHash) =>
        wallet.BlockModel.readonly().get(blockHash).items.map(_.headOption)
      } getOrElse {
        Future.successful(None)
      } map {(b) =>
        new Transaction {
          override def nonce: BigInt = BigInt(tx.nonce().get, 16)

          override def data: String = tx.data().get

          override def gas: Ether = Ether(tx.gas().get)

          override def block: Option[Block] = b.map(_.proxy)

          override def gasPrice: Ether = Ether(tx.gasPrice().get)

          override def from: String = tx.from().get

          override def to: String = tx.to().get

          override def hash: String = tx.hash().get

          override def value: Ether = Ether(tx.value().get)

          override def cumulativeGasUsed: Ether = Ether(tx.cumulativeGasUsed().get)

          override def receivedAt: Date = new Date(tx.receivedAt().get.getTime().toLong)
        }
      }
    } map {(tx) =>
      new Operation {
        override def `type`: String = model.operationType().get

        override def account: Account = IndexedDBBackedAccountClient.this

        override def transaction: Transaction = tx
      }
    }
  }

  override def countOperations(): Future[Long] = {
    wallet.OperationModel.readonly().exactly(index).openCursor("accountId").count
  }
}
