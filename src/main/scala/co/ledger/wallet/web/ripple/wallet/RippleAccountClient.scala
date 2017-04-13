package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ripple._

import scala.concurrent.Future

/**
  * Created by alix on 4/13/17.
  */
class RippleAccountClient extends Account {
  override def index: Int = ???

  override def wallet: Wallet = ???

  override def synchronize(): Future[Unit] = ???

  override def isSynchronizing(): Future[Boolean] = ???

  override def operations(limit: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override def rippleAccount(): Future[RippleAccount] = ???

  override def rippleAccountDerivationPath(): Future[DerivationPath] = ???

  override def balance(): Future[XRP] = ???
}
