package co.ledger.wallet.core.wallet.ripple.api

import co.ledger.wallet.core.concurrent.AsyncCursor
import co.ledger.wallet.core.net.HttpClient
import co.ledger.wallet.core.wallet.ripple._
import co.ledger.wallet.core.wallet.ripple.database.AccountRow

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.util.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Locale

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js
import scala.util.Try

/**
  * Created by alix on 4/14/17.
  */
class ApiAccountRestClient(http: HttpClient,
                                       private val accountRow: AccountRow = null
                                       ) {



  def balance(): Future[XRP] = {
      val request = http.get(s"/accounts/${
        accountRow.rippleAccount}/balances?currency=XRP")
      request.json map {
        case (json, response) =>
          if (response.statusCode == 500) {
            XRP.Zero
          } else {
            var value = json.getJSONArray("balances").getJSONObject(0)
              .getString("value")
            new XRP((BigDecimal(value) * BigDecimal(10).pow(6)).toBigInt())
          }
      }
  }

  def transactions(init: String): Future[Array[JsonTransaction]] = {
    val start = new Date(init)
    val dateLiteral = new js.Date(start.getTime + 1000).toJSON()
    var transactionsBuffer = ArrayBuffer[JsonTransaction]()
    def iterate(marker: String = ""): Future[Array[JsonTransaction]] = {
      var url = s"/accounts/${accountRow.rippleAccount}/transactions?type=Payment" +
        s"&descending=false&result=tesSUCCESS&start=${dateLiteral}"
      if (marker != "") {
        url += s"&marker=$marker"
      }
      var request = http.get(url)
      request.json flatMap {
        case (json, _) =>
          if (json.getInt("count") > 0) {
            val txs = json.getJSONArray("transactions")
            (0 until txs.length()) map { (index: Int) =>
              transactionsBuffer.append(new JsonTransaction(txs.getJSONObject(index)))
            }
          }
          if (json.has("marker")) {
            iterate(json.getString("marker"))
          } else {
            Future.successful(transactionsBuffer.toArray)
          }
      }
    }
    iterate()
  }

  def fees(): Future[XRP] = {
    val request = http.get(s"/network/fees?interval=ledger&limit=1&descending=true")
    request.json map {
      case (json, _) =>
        val fees = json.getJSONArray("rows").getJSONObject(0).getDouble("avg")
        new XRP((BigDecimal(fees) * BigDecimal(10).pow(6)).toBigInt())
    }
  }


  def account(address: String): Future[Boolean] = {
    val request = http.get(s"/accounts/$address")
    Try {
      request.json map {
        case (json, _) =>
          json.getString("result") == "success"
      }
    }.getOrElse(Future.failed(new Throwable()))
  }
}
