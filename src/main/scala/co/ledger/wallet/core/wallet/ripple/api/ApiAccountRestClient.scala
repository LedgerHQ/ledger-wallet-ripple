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
          val value = XRP(json.getJSONArray("balances").getJSONObject(0)
            .getString("value"))
          value
      }
  }

  def transactions(start: Date = new Date(2004,1,1)):
  Future[Array[JsonTransaction]] = {
    val request = http.get(s"/accounts/${accountRow.rippleAccount}/payment?currency=XRP" +
      s"&descending=false&start=${start.toString}") //TODO use calendar
    request.json map {
      case (json, _) =>
        val txs = json.getJSONArray("payments")
        (0 until txs.length()) map {(index: Int) =>
         new JsonTransaction(txs.getJSONObject(index))
        } toArray
    }
  }


}
