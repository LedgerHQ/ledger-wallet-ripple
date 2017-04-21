package co.ledger.wallet.web.ripple.wallet

import co.ledger.wallet.core.wallet.ripple.RippleAccount

/**
  * Created by alix on 4/21/17.
  */
class RipplePreparePayment(account: RippleAccount,
                   val payment: RipplePayment) {
  val address = account.toString
}
