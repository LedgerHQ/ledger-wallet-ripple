package co.ledger.wallet.web.ripple.wallet.database

import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.content.{AccountModel, TransactionModel, WalletDatabaseDeclaration}
import co.ledger.wallet.web.ripple.core.database.{DatabaseDeclaration, ModelCreator, QueryHelper}
import co.ledger.wallet.web.ripple.content

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
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
      model
    }
  }
  object OperationModel extends QueryHelper[content.OperationModel] with
    ModelCreator[content.OperationModel] {
    override def database: DatabaseDeclaration = DatabaseDeclaration
    override def creator: ModelCreator[content.OperationModel] = this
    override def newInstance(): content.OperationModel = new content
    .OperationModel()
  }
  object DatabaseDeclaration
    extends WalletDatabaseDeclaration(name) {
    override def models: Seq[QueryHelper[_]] = Seq(
      AccountModel,
      OperationModel
    )
  }
  def queryAccounts(): Future[Array[AccountModel]] = {
    AccountModel.readonly(password).cursor flatMap {(cursor) =>
      val buffer = new ArrayBuffer[AccountModel]
      def iterate(): Future[Array[AccountModel]] = {
        if (cursor.value.isEmpty){
          Future.successful(buffer.toArray)
        } else {
          buffer.append(cursor.value.get)
          cursor.continue() flatMap {(_) =>
            iterate()
          }
        }
      }
      iterate()
    }
  }
  def putAccount(account: AccountRow): Future[Unit] = {
    AccountModel.readwrite(password).add(AccountModel(account)).commit().map((_) =>())
  }
}

