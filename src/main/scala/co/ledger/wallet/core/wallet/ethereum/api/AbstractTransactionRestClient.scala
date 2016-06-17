package co.ledger.wallet.core.wallet.ethereum.api

import java.util.Date

import co.ledger.wallet.core.net.HttpClient
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
abstract class AbstractTransactionRestClient(http: HttpClient, blockRestClient: AbstractBlockRestClient) {

  def transactions(syncToken: String, ethereumAccounts: Array[String]): Future[TransactionsPage] = {
    http.get(s"/addresses/${ethereumAccounts.mkString(",")}/transactions")
        .header("X-LedgerWallet-SyncToken" -> syncToken).jsonArray map {
      case (json, _) =>
        val result = new ArrayBuffer[Transaction](json.length())
        for (index <- 0 until json.length()) {
          result += new JsonTransaction(json.getJSONObject(index))
        }
        TransactionsPage(result.toArray, false)
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
    override val hash: String = json.getString("hash")
    override val receivedAt: Date = stringToDate(json.getString("received_at"))
    override val value: Ether = Ether(json.getString("value"))
    override val gas: Ether = Ether(json.getString("gas"))
    override val gasPrice: Ether = Ether(json.getString("gas_price"))
    override val cumulativeGasUsed: Ether = Ether(json.getString("cumulative_gas_used"))
    override val from: String = json.getString("from")
    override val to: String = json.getString("to")
    override val block: Option[Block] = Option(json.optJSONObject("block")).map((b) => blockRestClient.jsonToBlock(b))
  }

  case class TransactionsPage(transactions: Array[Transaction], isTruncated: Boolean)

}