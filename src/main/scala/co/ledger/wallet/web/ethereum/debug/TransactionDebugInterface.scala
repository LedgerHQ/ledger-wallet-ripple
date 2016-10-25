package co.ledger.wallet.web.ethereum.debug

import co.ledger.wallet.core.device.Device
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.utils.{DerivationPath, HexUtils}
import co.ledger.wallet.core.wallet.ethereum.{Ether, EthereumAccount}
import co.ledger.wallet.core.wallet.ethereum.api.AbstractApiWalletClient
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js.UndefOr
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success}

/**
  *
  * TransactionDebugInterface
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 18/10/2016.
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

trait TransactionDebugInterface extends BaseDebugInterface {

  def wallet = SessionService.instance.currentSession.get.wallet

  @JSExport
  def getAddress(path: String): Future[String] = {
    DeviceService.instance.lastConnectedDevice() flatMap {(d) =>
      LedgerApi(d).derivePublicAddress(DerivationPath(path), false)
    } map {(address) =>
      log(s"Derive path [$path] => ${address.account.toChecksumString}")
      address.account.toChecksumString
    }
  }

  @JSExport
  def getBalance(address: String): Future[String] = {
    wallet.asInstanceOf[AbstractApiWalletClient].transactionRestClient.getAccountBalance(address) map {(balance) =>
      log(s"Balance [$address] => ${balance.toEther.toString()} ETH")
      balance.toEther.toString()
    }
  }

  def getNonce(address: String): Future[Long] = {
    wallet.asInstanceOf[AbstractApiWalletClient].transactionRestClient.getAccountNonce(address) map {(nonce) =>
      log(s"Nonce [$address] => ${nonce.toLong}")
      nonce.toLong
    }
  }

  @JSExport
  def signTransaction(path: String,
                      to: String,
                      value: String,
                      gasLimit: UndefOr[String],
                      gasPrice: UndefOr[String],
                      data: UndefOr[String]) = {
    var device: LedgerApi = null
    var nonce: Long = 0L
    val limit = gasLimit.getOrElse("210000")
    val sentData = data.map(HexUtils.decodeHex).getOrElse(Array.empty[Byte])
    getGasPrice() flatMap {(p) =>
      val sentGasPrice = gasPrice.getOrElse(p.toString())
      getAddress(path) flatMap {(address) =>
        getNonce(address) map {(n) =>
          nonce = n
        } flatMap {(_) =>
          DeviceService.instance.lastConnectedDevice()
        } flatMap {(d) =>
          LedgerApi(d).signTransaction(
            BigInt(nonce),
            BigInt(sentGasPrice),
            BigInt(limit),
            DerivationPath(path),
            EthereumAccount(to),
            (BigDecimal(value.replace(',', '.').replace(" ", "")) * BigDecimal(10).pow(18)).toBigInt(),
            sentData
          )
        }
      }
    } onComplete {
      case Success(v) =>
        log("Signed TX: "+ HexUtils.encodeHex(v.signedTx))
      case Failure(ex) => ex.printStackTrace()
    }
  }

  @JSExport
  def pushTransaction(tx: String): Unit = {
    wallet.pushTransaction(HexUtils.decodeHex(tx)) onComplete {
      case Success(_) =>
        log("Transaction pushed")
      case Failure(ex) =>
        ex.printStackTrace()
    }
  }

  @JSExport
  def getGasPrice(): Future[BigInt] = {
    wallet.estimatedGasPrice() map {(price) =>
      log(s"Estimated gas price: ${price.toBigInt.toString()}")
      price.toBigInt
    }
  }

}
