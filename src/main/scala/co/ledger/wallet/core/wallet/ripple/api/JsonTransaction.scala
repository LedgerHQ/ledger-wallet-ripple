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

  override def value: XRP = XRP(obj.getString("delivered_amount"))

  override def fee: XRP = XRP(obj.getString("transaction_cost"))

  override def account: RippleAccount = RippleAccount(obj.getString("source"))

  override def destination: RippleAccount = RippleAccount(obj.getString
  ("destination"))

  override def receivedAt: Date = new Date(new js.Date(obj.getString("date"))
    .getTime.toLong)

  override def hash: String = obj.getString("tx_hash")

  override def height: Option[Long] = Some(obj.getLong("ledger_index"))
}
