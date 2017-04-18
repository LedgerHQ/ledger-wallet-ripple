package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.services.SessionService
import co.ledger.wallet.web.ripple.wallet.database.RippleDatabase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by alix on 4/13/17.
  */
class RippleWalletClient(override val name: String,
                        override val password: Option[String],
                        provider: RippleAccountProvider,
                        chain: SessionService.RippleChainIdentifier
                        ) extends Wallet with RippleDatabase {
  private def init(): Future[Array[RippleAccountClient]] = {
    _accounts.getOrElse({
      _accounts = Some(queryAccounts() flatMap { (accounts) =>
        if (accounts.isEmpty) {
          createNewAccount(0).map(Array(_))
        } else {
          Future.successful(accounts)
        }
      } map { (accounts) =>
        accounts map { (account) =>
          new RippleAccountClient(this, account)
        }
      })
      _accounts.get
    })
  }

  private def createNewAccount(index: Int): Future[AccountRow] = {
    provider.getRippleAccount(DerivationPath(s"44'/${chain.coinType}'/$index'/0/0"))
    .flatMap {(account) =>
        val row = new AccountRow(index, account.toString, XRP.Zero)
        putAccount(row).map(_ => row)
    }
  }

  private var _accounts: Option[Future[Array[RippleAccountClient]]] = None

  override def bip44CoinType: String = chain.coinType

  override def coinPathPrefix: String = chain.pathPrefix

  override def account(index: Int): Future[Account] = {
    init() map {(accounts) =>
      accounts(index)}
  }
  override def accounts(): Future[Array[Account]] = {
    init() map {(accounts) =>
      accounts.asInstanceOf[Array[Account]]}
  }

  override def balance(): Future[XRP] = {
    println("called")
    accounts() flatMap { (accounts) =>
      println("account")
      val futureBalances = accounts map { (account) =>
        account.balance()
      }
      println("mid")
      println(futureBalances)
      Future.sequence(futureBalances.toSeq)
    } map {(balances) =>
      println("beforefold")
      balances.foldLeft(XRP.Zero)(_ + _)
    }
  }
  override def synchronize(): Future[Unit] = {
    println("Synchronizing")
    _synchronizationFuture.getOrElse({
      _synchronizationFuture = Some(
        accounts() flatMap {(accounts) =>
          println("Synchronizing1")
          Future.sequence(accounts.map(_.synchronize()).toSeq)
        } map { _ => _synchronizationFuture = None}
      )
      _synchronizationFuture.get
    })
  }

  override def isSynchronizing(): Future[Boolean] = Future.successful(
    _synchronizationFuture.nonEmpty
  )

  override def pushTransaction(transaction: Array[Byte]): Future[Unit] = ???

  override def operations(from: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override val eventEmitter: EventEmitter = new JsEventEmitter()

  override def stop(): Unit = ???

  private var _synchronizationFuture: Option[Future[Unit]] = None

}
