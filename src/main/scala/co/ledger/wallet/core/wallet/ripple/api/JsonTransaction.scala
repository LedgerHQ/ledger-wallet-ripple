package co.ledger.wallet.core.wallet.ripple.api

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import co.ledger.wallet.core.wallet.ripple.{RippleAccount, Transaction, XRP}
import org.json.JSONObject
import org.widok.moment.Moment

import scala.scalajs.js
/**
  * Created by alix on 4/14/17.
  */
class JsonTransaction(obj: JSONObject) extends Transaction {

  override def value: XRP = XRP(obj
      .getJSONObject("meta").getString("delivered_amount"))

  override def fee: XRP = XRP(obj
    .getJSONObject("tx").getString("Fee"))

  override def account: RippleAccount = RippleAccount(obj
    .getJSONObject("tx").getString("Account"))

  override def destination: RippleAccount = RippleAccount(obj
    .getJSONObject("tx").getString("Destination"))

  override def receivedAt: Date = new Date((obj.getJSONObject("tx").getLong("date")*1000)+946684800000L)

  override def hash: String = obj.getJSONObject("tx").getString("hash")

  override def ledger: Long = obj.getJSONObject("tx").getLong("ledger_index")
}
