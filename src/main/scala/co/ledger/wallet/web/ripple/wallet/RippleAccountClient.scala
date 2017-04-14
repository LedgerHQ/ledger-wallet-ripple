package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.database.AccountRow

import scala.concurrent.Future

/**
  * Created by alix on 4/13/17.
  */
class RippleAccountClient(walletClient: RippleWalletClient,
                         row: AccountRow) extends Account {
  override def index: Int = row.index

  override def wallet: Wallet = walletClient.asInstanceOf[Wallet]

  override def synchronize(): Future[Unit] = ???

  override def isSynchronizing(): Future[Boolean] = ???

  override def operations(limit: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override def rippleAccount(): Future[RippleAccount] =
    Future.successful(RippleAccount(row.rippleAccount))

  override def rippleAccountDerivationPath(): Future[DerivationPath] =
    Future.successful(DerivationPath(s"44'/${walletClient
      .bip44CoinType}'/$index'/0/0"))

  override def balance(): Future[XRP] =
}


