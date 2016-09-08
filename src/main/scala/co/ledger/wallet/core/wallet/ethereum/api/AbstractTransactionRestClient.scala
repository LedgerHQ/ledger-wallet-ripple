package co.ledger.wallet.core.wallet.ethereum.api

import java.util.Date

import co.ledger.wallet.core.net.HttpClient
import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.core.wallet.ethereum.{Block, Ether, Transaction}
import org.json.JSONObject

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  *
  * TransactionRestClient
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 16/06/2016.
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
abstract class AbstractTransactionRestClient(http: HttpClient, val blockRestClient: AbstractBlockRestClient) {

  def transactions(syncToken: String, ethereumAccounts: Array[String], blockHash: Option[String]): Future[TransactionsPage] = {
    val request = http.get(s"/addresses/${ethereumAccounts.mkString(",")}/transactions")
        .header("X-LedgerWallet-SyncToken" -> syncToken)
    if (blockHash.isDefined)
      request.param("blockHash" -> blockHash.get)
    request.json map {
      case (json, _) =>
        val txs = json.getJSONArray("txs")
        val result = new ArrayBuffer[Transaction](txs.length())
        for (index <- 0 until txs.length()) {
          result += new JsonTransaction(txs.getJSONObject(index))
        }
        TransactionsPage(result.toArray, json.getBoolean("truncated"))
    }
  }

  def getAccountNonce(ethereumAccount: String): Future[BigInt] = {
    http.get(s"/addresses/$ethereumAccount/nonce").jsonArray map {
      case (json, _) =>
        BigInt(json.getJSONObject(0).getLong("nonce"))
    }
  }

  def pushTransaction(signedTransaction: Array[Byte]): Future[Unit] = {
    http.post("/transactions/send").body(Map(
      "tx" -> HexUtils.encodeHex(signedTransaction)
    )).noResponseBody map {(result) =>
      ()
    }
  }

  def getEstimatedGasPrice(): Future[Ether] = {
    http.get("/fees").json map {
      case (json, _) =>
        Ether(json.getString("gas_price"))
    }
  }

  def getAccountBalance(ethereumAccount: String): Future[Ether] = {
    http.get(s"/addresses/$ethereumAccount/balance").jsonArray map {
      case (json, _) =>
        Ether(json.getJSONObject(0).getString("balance"))
    }
  }

  def stringToDate(string: String): Date
  def obtainSyncToken(): Future[String] = http.get("/syncToken").json map(_._1.getString("token"))
  def deleteSyncToken(syncToken: String): Future[Unit] = {
    http.delete("/syncToken")
        .header("X-LedgerWallet-SyncToken" -> syncToken)
        .noResponseBody
        .map((_) => ())
  }

  class JsonTransaction(json: JSONObject) extends Transaction {
    override def nonce: BigInt = BigInt(json.getString("nonce").substring(2), 16)
    override def data: String = json.getString("input")
    override val hash: String = json.getString("hash")
    override val receivedAt: Date = stringToDate(json.getString("received_at"))
    override val value: Ether = Ether(json.getString("value"))
    override val gas: Ether = Ether(json.getString("gas"))
    override val gasUsed: Ether = Ether(json.optString("gas_used", "0"))
    override val gasPrice: Ether = Ether(json.getString("gas_price"))
    override val cumulativeGasUsed: Ether = Ether(json.optString("cumulative_gas_used", "0"))
    override val from: String = json.getString("from")
    override val to: String = json.getString("to")
    override val block: Option[Block] = Option(json.optJSONObject("block")).map((b) => blockRestClient.jsonToBlock(b))
  }

  case class TransactionsPage(transactions: Array[Transaction], isTruncated: Boolean)

}