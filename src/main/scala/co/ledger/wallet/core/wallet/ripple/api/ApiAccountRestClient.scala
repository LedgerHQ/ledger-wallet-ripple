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

/**
  * Created by alix on 4/14/17.
  */
class ApiAccountRestClient(http: HttpClient,
                                       private val accountRow: AccountRow
                                       ) {



  def balance(): Future[XRP] = {
      val request = http.get(s"/accounts/${
        accountRow.rippleAccount}/balances?currency=XRP")
      request.json map {
        case (json, _) =>
          var value = json.getJSONArray("balances").getJSONObject(0)
            .getString("value")
          new XRP((BigDecimal(value) * BigDecimal(10).pow(6)).toBigInt())
      }
  }

  def transactions(start: Date = new Date(0)):
  Future[Array[JsonTransaction]] = {
    val dateLiteral = new js.Date(start.getTime).toJSON()
    val request = http.get(s"/accounts/${accountRow
      .rippleAccount}/transactions?type=Payment" +
      s"&descending=false&result=tesSUCCESS&start=${dateLiteral}")
    request.json map {
      case (json, _) =>
        val txs = json.getJSONArray("transactions")
        (0 until txs.length()) map {(index: Int) =>
         new JsonTransaction(txs.getJSONObject(index))
        } toArray
    }
  }


}
