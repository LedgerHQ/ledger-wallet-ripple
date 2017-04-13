package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.device.utils.EventEmitter
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.services.SessionService
import co.ledger.wallet.web.ripple.wallet.database.RippleDatabase
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
      _accounts = Some(queryAccounts() flatMap {(accounts) =>
        if (accounts.isEmpty) {
          createNewAccount(0).map(Array(_))
        } else {
          Future.successful(accounts)
        }
      } map {(accounts) =>
        accounts map {(account) =>
          new RippleAccountClient(this, account)
        }
      })
      _accounts.get
    })
  }

  private def createNewAccount(index: Int): Future[AccountRow] = ???

  private var _accounts: Option[Future[Array[RippleAccountClient]]] = None

  override def bip44CoinType: String = chain.coinType

  override def coinPathPrefix: String = chain.pathPrefix

  override def account(index: Int): Future[Account] = ???

  override def accounts(): Future[Array[Account]] = ???

  override def balance(): Future[XRP] = ???

  override def synchronize(): Future[Unit] = ???

  override def isSynchronizing(): Future[Boolean] = ???

  override def pushTransaction(transaction: Array[Byte]): Future[Unit] = ???

  override def operations(from: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override val eventEmitter: EventEmitter = new JsEventEmitter()

  override def stop(): Unit = ???
}
