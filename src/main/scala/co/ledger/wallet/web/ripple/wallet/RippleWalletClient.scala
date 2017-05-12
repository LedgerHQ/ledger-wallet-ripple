package co.ledger.wallet.web.ripple.wallet

import java.net.URI

import co.ledger.wallet.core.concurrent.{AbstractAsyncCursor, AsyncCursor}
import co.ledger.wallet.core.device.utils.{EventEmitter, EventReceiver}
import co.ledger.wallet.core.net.WebSocketFactory
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.core.wallet.ripple.Wallet.{StartSynchronizationEvent, StopSynchronizationEvent}
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.api.WebSocketRipple
import co.ledger.wallet.core.wallet.ripple.database.AccountRow
import co.ledger.wallet.core.wallet.ripple.events.NewTransaction
import co.ledger.wallet.web.ripple.core.event.JsEventEmitter
import co.ledger.wallet.web.ripple.core.net.JsWebSocketFactory
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
    if (_stopped)
      Future.failed(new Exception("Client is stopped"))
    else {
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
  }

  private def createNewAccount(index: Int): Future[AccountRow] = {
    provider.getRippleAccount(DerivationPath(s"44'/${chain.coinType}'/$index'/0/0"))
    .flatMap {(account) =>
        val row = new AccountRow(index, account.toString, XRP.Zero)
        putAccount(row).map(_ => row)
    }
  }

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
    accounts() flatMap { (accounts) =>
      val futureBalances = accounts map { (account) =>
        account.balance()
      }
      Future.sequence(futureBalances.toSeq)
    } map {(balances) =>
      balances.foldLeft(XRP.Zero)(_ + _)
    }
  }
  override def synchronize(): Future[Unit] = {
    println("synchro future=", _synchronizationFuture)
    if (_synchronizationFuture.isEmpty || (_synchronizationFuture.isDefined && _synchronizationFuture.get.isCompleted && _synchronizationFuture.get.value.get.isFailure)){
      eventEmitter.emit(StartSynchronizationEvent())
      _synchronizationFuture = Some(
        accounts() flatMap {(accounts) =>
          Future.sequence(accounts.map(_.synchronize()).toSeq)
        } map { _ =>
          _synchronizationFuture = None
          eventEmitter.emit(StopSynchronizationEvent())
        }
      )
      _synchronizationFuture.get
    } else {
      _synchronizationFuture.get
    }
  }

  override def isSynchronizing(): Future[Boolean] = Future.successful(
    _synchronizationFuture.nonEmpty
  )

  override def pushTransaction(transaction: Array[Byte]): Future[Unit] = ???

  override val eventEmitter: EventEmitter = new JsEventEmitter()

  override def operations(from: Int, batchSize: Int): Future[AsyncCursor[Operation]] = ???

  override def stop(): Unit = {
    init() foreach {(_) =>
      _stopped = true
      _webSocketRipple.get.stop()
    }
  }
  private def websocketFactory: WebSocketFactory = new JsWebSocketFactory(new URI(s"wss://s1.ripple.com"))

  private var _webSocketRipple: Option[WebSocketRipple] = None

  private var _accounts: Option[Future[Array[RippleAccountClient]]] = None

  private var _stopped = false

  private var _synchronizationFuture: Option[Future[Unit]] = None

  accounts().map({(accounts) =>
    _webSocketRipple = Some(new WebSocketRipple(websocketFactory, accounts.map(_.toString), this))
    _webSocketRipple.get.start()
  })


}
