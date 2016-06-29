package co.ledger.wallet.web.ethereum.wallet

import co.ledger.wallet.core.wallet.ethereum.{Block, Operation, Transaction}
import co.ledger.wallet.core.wallet.ethereum.database.{AccountRow, DatabaseBackedWalletClient}
import co.ledger.wallet.web.ethereum.content
import co.ledger.wallet.web.ethereum.content.{AccountModel, OperationModel, TransactionModel}
import co.ledger.wallet.web.ethereum.core.database.{DatabaseDeclaration, ModelCreator, QueryHelper}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js
import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  * IndexedDBBackedWalletClient
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
trait IndexedDBBackedWalletClient extends DatabaseBackedWalletClient {

  // Constructor
  // TODO: Remove database delete
  // DatabaseDeclaration.delete()
  private val connection = DatabaseDeclaration.obtainConnection()
  // \Constructor

  override def putBlock(block: Block): Future[Unit] = {
    BlockModel.readwrite().add(BlockModel(block)).commit().map((_) => ()).recover({case all => ()})
  }

  override protected def putAccount(accountRow: AccountRow): Future[Unit] = {
    AccountModel.readwrite().add(AccountModel(accountRow)).commit().map((_) => ())
  }

  override def startDatabaseTransaction(): Unit = () // Noop

  override def commitDatabaseTransaction(): Unit = () // Noop

  override protected def queryAccounts(from: Int, to: Int): Future[Array[AccountRow]] = {
    val result = new ArrayBuffer[AccountRow]()
    AccountModel.readonly().cursor flatMap {(cursor) =>
      cursor.advance(from) flatMap {(_) =>
        def iterate(): Future[Array[AccountRow]] = {
          if (cursor.value.isEmpty || result.length >= to - from)
            Future.successful(result.toArray)
          else {
            result.append(cursor.value.get.proxy)
            cursor.continue().flatMap((_) => iterate())
          }
        }
        iterate()
      }
    }
  }

  override protected def queryLastBlock(): Future[Block] = {
    BlockModel.readonly().reverse().cursor map {(cursor) =>
      cursor.value.map(_.proxy).orNull
    }
  }

  /**
    * Insert or update a transaction in the database
    *
    * @param transaction
    * @return
    */
  override def putTransaction(transaction: Transaction): Future[Unit] = {
    TransactionModel.readwrite().openCursor().exactly(transaction.hash).writeCursor flatMap {(cursor) =>
      if (cursor.value.isEmpty) {
        TransactionModel.readwrite().add(TransactionModel(transaction)).commit()
      } else {
        cursor.update(TransactionModel(transaction))
      }
    } map {(_) =>
      ()
    }
  }

  override def putTransactions(transactions: Array[Transaction]): Future[Unit] = {
    def iterate(index: Int): Future[Unit] = {
      if (index >= transactions.length) {
        Future.successful()
      } else {
        putTransaction(transactions(index)) flatMap {(_) =>
          transactions(index).block match {
            case Some(block) => putBlock(block)
            case None => Future.successful()
          }
        } flatMap {(_) =>
          iterate(index + 1)
        }
      }
    }
    iterate(0)
  }

  override def putOperation(operation: Operation): Future[Unit] = {
    OperationModel.readwrite().openCursor().exactly(operation.uid).writeCursor flatMap {(cursor) =>
      if (cursor.value.isEmpty) {
        OperationModel.readwrite().add(OperationModel(operation)).commit()
      } else {
        cursor.update(OperationModel(operation))
      }
    } map {(_) =>
      ()
    }
  }

  override def putOperations(operations: Array[Operation]): Future[Unit] = {
    def iterate(index: Int): Future[Unit] = {
      if (index >= operations.length) {
        Future.successful()
      } else {
        putOperation(operations(index))  flatMap {(_) =>
          iterate(index + 1)
        }
      }
    }
    iterate(0)
  }

  override protected def queryTransaction(hash: String): Future[Array[Transaction]] = ???
  object BlockModel extends QueryHelper[content.BlockModel] with ModelCreator[content.BlockModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.BlockModel] = this
    override def newInstance(): content.BlockModel = new content.BlockModel()
    def apply(block: Block): content.BlockModel = {
      val b = new content.BlockModel
      b.hash.set(block.hash)
      b.height.set(block.height)
      b.time.set(new js.Date(block.time.getTime))
      b
    }
  }

  object AccountModel extends QueryHelper[content.AccountModel] with ModelCreator[content.AccountModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.AccountModel] = this
    override def newInstance(): content.AccountModel = new content.AccountModel()

    def apply(row: AccountRow): content.AccountModel = {
      val a = new content.AccountModel
      a.index.set(row.index)
      a.ethereumAccount.set(row.ethereumAccount)
      a
    }
  }

  object TransactionModel extends QueryHelper[content.TransactionModel] with ModelCreator[content.TransactionModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[TransactionModel] = this
    override def newInstance(): TransactionModel = new content.TransactionModel()
    def apply(transaction: Transaction) = {
      val t = new content.TransactionModel
      t.hash.set(transaction.hash)
      t.blockHash.set(transaction.block.map(_.hash).orNull)
      t.to.set(transaction.to)
      t.from.set(transaction.from)
      t.gas.set(transaction.gas.toString)
      t.gasPrice.set(transaction.gasPrice.toString)
      t.cumulativeGasUsed.set(transaction.cumulativeGasUsed.toString)
      t.receivedAt.set(new js.Date(transaction.receivedAt.getTime))
      t.value.set(transaction.value.toString)
      t.nonce.set(transaction.nonce.toString(16).reverse.padTo(64, '0').reverse.toString)
      t.data.set(transaction.data)
      t
    }
  }

  object OperationModel extends QueryHelper[content.OperationModel] with ModelCreator[content.OperationModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[OperationModel] = this
    override def newInstance(): OperationModel = new content.OperationModel()

    def apply(operation: Operation): content.OperationModel = {
      val o = new content.OperationModel
      o.uid.set(operation.uid)
      o.accountId.set(operation.account.index)
      o.operationType.set(operation.`type`)
      o.transactionHash.set(operation.transaction.hash)
      o.time.set(new js.Date(operation.transaction.receivedAt.getTime))
      o
    }
  }

  object DatabaseDeclaration extends content.WalletDatabaseDeclaration(name) {
    override def models: Seq[QueryHelper[_]] = Seq(
      BlockModel,
      AccountModel,
      TransactionModel,
      OperationModel,
      OperationModel
    )
  }
}
