package co.ledger.wallet.web.ethereum.controllers.wallet

import co.ledger.wallet.web.ethereum.services.WindowService

/**
  * Created by pollas_p on 03/05/2016.
  */
trait WalletController {
  val windowService: WindowService

  windowService.showNavigationBar()
}
