package co.ledger.wallet.core.wallet.ripple.api

import co.ledger.wallet.core.net.HttpClient
import co.ledger.wallet.core.wallet.ripple.{Account, Wallet, XRP}
import co.ledger.wallet.core.wallet.ripple.database.AccountRow

import scala.concurrent.Future

/**
  * Created by alix on 4/14/17.
  */
abstract class AbstractApiAccountClient(http: HttpClient,
                                         override val wallet: Wallet,
                                       private val accountRow: AccountRow
                                       ) extends Account {
  override def balance(): Future[XRP] = {
      val request = http.get(s"https://data.ripple.com/v2/accounts/${
        accountRow.rippleAccount}/balances?currency=XRP")
      request.json map {
        case (json, _) =>
          val value = XRP(json.getJSONArray("balances").getJSONObject(0)
            .getString("value"))
          value
      }
  }
}
