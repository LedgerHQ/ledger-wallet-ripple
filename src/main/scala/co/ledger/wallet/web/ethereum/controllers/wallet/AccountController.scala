package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.JQLite
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.utils.EventReceiver
import co.ledger.wallet.core.wallet.ethereum.Operation
import co.ledger.wallet.core.wallet.ethereum.Wallet.{NewOperationEvent, StartSynchronizationEvent, StopSynchronizationEvent}
import co.ledger.wallet.web.ethereum.components.SnackBar
import co.ledger.wallet.web.ethereum.i18n.DateFormat
import co.ledger.wallet.web.ethereum.services.{SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.{JSON, timers}
import scala.util.{Failure, Success}
/**
  *
  * OperationController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 03/05/2016.
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
class AccountController(override val windowService: WindowService,
                        sessionService: SessionService,
                        $scope: Scope,
                        $element: JQLite,
                        $routeParams: js.Dictionary[String])
  extends Controller with WalletController with EventReceiver {
  println(JSON.stringify($routeParams))

  val accountId = $routeParams("id").toInt

  def refresh(): Unit = {
    sessionService.currentSession.get.wallet.synchronize()
  }

  var isRefreshing = false

  var operations = js.Array[js.Dictionary[js.Any]]()

  private var reloadOperationNonce = 0
  def reloadOperations(): Unit = {
    operations = js.Array[js.Dictionary[js.Any]]()
    reloadOperationNonce += 1
    val nonce = reloadOperationNonce
    sessionService.currentSession.get.wallet.account(accountId).flatMap {
      _.operations(-1, 10)
    } foreach {cursor =>
      var isLoading = false
      def loadMore(): Unit = {
        isLoading = true
        cursor.loadNextChunk() andThen {
          case Success(ops) =>
            ops foreach {(op) =>
              operations.push(js.Dictionary[js.Any](
                "uid" -> op.uid,
                "hash" -> op.transaction.hash,
                "date" -> DateFormat.formatStandard(op.transaction.receivedAt),
                "amount" -> ((if (op.`type` == Operation.SendType) "-" else "+") + op.transaction.value.toEther.toString()),
                "isSend" -> (op.`type` == Operation.SendType)
              ))
            }
            $scope.$digest()
          case Failure(ex) => ex.printStackTrace()
        } andThen {
          case all =>
            isLoading = false
            refresh()
        }
      }

      def refresh() = {
        val top = $element.asInstanceOf[js.Dynamic].scrollTop().asInstanceOf[Double]
        val scrollHeight = $element.asInstanceOf[js.Dynamic].height().asInstanceOf[Double]
        val height = $element(0).asInstanceOf[js.Dynamic].scrollHeight.asInstanceOf[Double]
        if (top + scrollHeight >= height * 0.90) {
          if (!isLoading && reloadOperationNonce == nonce && cursor.loadedChunkCount < cursor.chunkCount) {
            loadMore()
          }
        }
      }

      $element.asInstanceOf[js.Dynamic].scroll({() =>
        refresh()
      })

      js.Dynamic.global.$(js.Dynamic.global.window).resize({() =>
        refresh()
      })
      loadMore()
    }
  }

  var balance = "-"
  def reloadBalance(): Unit = {
    sessionService.currentSession.get.wallet.account(accountId) flatMap {(account) =>
      account.balance()
    } onComplete {
      case Success(b) =>
        balance = b.toEther.toString()
        $scope.$digest()
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  def openTransactionDetails(hash: String): Unit = {
    println(s"Open $hash")
    js.Dynamic.global.open(s"http://etherscan.io/tx/$hash")
  }

  sessionService.currentSession.get.wallet.eventEmitter.register(this)

  $scope.$on("$destroy", {() =>
    sessionService.currentSession.foreach(_.wallet.eventEmitter.unregister(this))
  })

  reloadBalance()
  reloadOperations()

  import timers._
  override def receive: Receive = {
    case StartSynchronizationEvent() =>
      isRefreshing = true
      setTimeout(0) {
        $scope.$digest()
      }
    case StopSynchronizationEvent() =>
      isRefreshing = false
      setTimeout(0) {
        $scope.$digest()
      }
    case NewOperationEvent(account, event) =>
      if (account.index == accountId) {
        reloadOperations()
        reloadBalance()
      }
    case drop =>
  }

  sessionService.currentSession.get.wallet.isSynchronizing() foreach {(sync) =>
    isRefreshing = sync
    setTimeout(0) {
      $scope.$digest()
    }
  }

}

object AccountController {

  def init(module: RichModule) = {
    module.controllerOf[AccountController]("AccountController")
  }
  
}