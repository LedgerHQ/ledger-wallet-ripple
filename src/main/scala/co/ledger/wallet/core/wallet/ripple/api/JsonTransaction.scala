package co.ledger.wallet.core.wallet.ripple.api

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import co.ledger.wallet.core.wallet.ripple.{RippleAccount, Transaction, XRP}
import org.json.JSONObject

/**
  * Created by alix on 4/14/17.
  */
class JsonTransaction(obj: JSONObject) extends Transaction {
  val format = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ", Locale.ENGLISH)

  override def value: XRP = XRP(obj.getString("delivered_amount"))

  override def fee: XRP = XRP(obj.getString("transaction_cost"))

  override def account: RippleAccount = RippleAccount(obj.getString("source"))

  override def destination: RippleAccount = RippleAccount(obj.getString
  ("destination"))

  override def receivedAt: Date = format.parse(obj.getString("date"))

  override def hash: String = obj.getString("tx_hash")

  override def height: Option[Long] = Some(obj.getLong("ledger_index"))
}
