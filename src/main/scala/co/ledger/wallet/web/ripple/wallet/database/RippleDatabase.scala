package co.ledger.wallet.web.ripple.wallet.database

import java.text.SimpleDateFormat
import java.util.Locale

import co.ledger.wallet.core.wallet.ripple.{Account, Operation, Transaction, XRP}
import co.ledger.wallet.core.wallet.ripple.api.JsonTransaction
import co.ledger.wallet.core.wallet.ripple.database.{AccountRow, DatabaseOperation}
import co.ledger.wallet.web.ripple.content.{AccountModel, OperationModel, TransactionModel, WalletDatabaseDeclaration}
import co.ledger.wallet.web.ripple.core.database.{DatabaseDeclaration, ModelCreator, QueryHelper}
import co.ledger.wallet.web.ripple.content

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.scalajs.js

/**
  * Created by alix on 4/13/17.
  */
trait RippleDatabase {
  def name: String
  def password: Option[String]
  object AccountModel extends QueryHelper[content.AccountModel] with
    ModelCreator[content.AccountModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.AccountModel] = this
    override def newInstance(): content.AccountModel = new content
    .AccountModel()
    def apply(account: AccountRow): AccountModel = {
      val model = new AccountModel()
      model.index.set(account.index)
      model.rippleAccount.set(account.rippleAccount)
      model.balance.set(account.balance.toString)
      model
    }
  }

  object OperationModel extends QueryHelper[content.OperationModel] with
    ModelCreator[content.OperationModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.OperationModel] = this
    override def newInstance(): content.OperationModel = new content
    .OperationModel()
    def apply(account: AccountRow, transaction: Transaction): OperationModel = {
      val model = new OperationModel()
      model.uid.set(transaction.hash.concat(account.rippleAccount))
      model.accountId.set(account.index)
      model.operationType.set("payment")
      model.time.set(new js.Date(transaction.receivedAt.getTime))
      model.transactionHash.set(transaction.hash)
      model
    }
  }

  object TransactionModel extends QueryHelper[content.TransactionModel] with
    ModelCreator[content.TransactionModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.TransactionModel] = this
    override def newInstance(): content.TransactionModel = new content
    .TransactionModel()
    def apply(transaction: Transaction): TransactionModel = {
      val model = new TransactionModel()
      model.hash.set(transaction.hash)
      model.height.set(transaction.height.get)
      model.destination.set(transaction.destination.toString)
      model.fee.set(transaction.fee.toBigInt.toLong)
      model.value.set(transaction.value.toBigInt.toLong)
      model.receivedAt.set(new js.Date(transaction.receivedAt.getTime))
      model.account.set(transaction.account.toString)
      model
    }
  }

  object DatabaseDeclaration
    extends WalletDatabaseDeclaration(name) {
    override def models: Seq[QueryHelper[_]] = Seq(
      AccountModel,
      OperationModel,
      TransactionModel
    )
  }

  def queryAccounts(): Future[Array[AccountRow]] = {
    AccountModel.readonly(password).cursor flatMap {(cursor) =>
      val buffer = new ArrayBuffer[AccountRow]
      def iterate(): Future[Array[AccountRow]] = {
        if (cursor.value.isEmpty){
          Future.successful(buffer.toArray)
        } else {
          val model = cursor.value.get
          buffer.append(new AccountRow(model.index().get, model.rippleAccount
          ().get, XRP(model.balance().get)))
          cursor.continue() flatMap {(_) =>
            iterate()
          }
        }
      }
      iterate()
    }
  }
  case class BadAccountIndex() extends Exception

  def queryAccount(index: Int): Future[AccountRow] = {
    AccountModel.readonly(password).cursor flatMap {(cursor) =>
      cursor.advance(index) map {_ =>
        if (cursor.value.isEmpty) {
          throw BadAccountIndex()
        } else {
          val model = cursor.value.get
          new AccountRow(model.index().get, model.rippleAccount
          ().get, XRP(model.balance().get))
        }
      }
    }
  }

  def putAccount(account: AccountRow): Future[Unit] = {
    AccountModel.readwrite(password).put(AccountModel(account)).commit().map(
      (_) =>())
  }

  def putOperation(account: AccountRow, transaction: Transaction): Future[Unit]
  = {
    OperationModel.readwrite(password).put(this.OperationModel(account,transaction))
      .commit().map((_) =>())
  }

  def putTransaction(transaction: Transaction): Future[Unit] = {
    TransactionModel.readwrite(password).put(TransactionModel(transaction))
      .commit().map((_) =>())
  }

  def countOperations(index: Int = 0): Future[Long] = {
    OperationModel.readonly(password).exactly(index).openCursor("accountId")
      .count
  }

  def queryOperations(from: Int, to: Int, account: Account): Future[Array[Operation]] = {
    OperationModel.readonly(password).openCursor("time").reverse().cursor
      .flatMap({(cursor) =>
        cursor.advance(from)
        val buffer = new ArrayBuffer[(String, (TransactionModel) => DatabaseOperation)]
        def iterate(): Future[Array[Operation]] = {
          if (cursor.value.isEmpty || buffer.length >= (to-from)){
            val bufferOperation = new ArrayBuffer[Operation]
            buffer foreach { case (hash, function) =>
              TransactionModel.readonly(password).exactly(hash).openCursor("hash")
                .cursor map {(cursor) =>
                  bufferOperation.append(function(cursor.value.get))
              }
            }
            Future.successful(bufferOperation.toArray)
          } else {
            val model = cursor.value.get
            buffer.append((model.transactionHash().get,
              DatabaseOperation(model)(account)(_)))
            cursor.continue() flatMap {(_) =>
              iterate()
            }
          }
        }
        iterate()
      })
  }
}

