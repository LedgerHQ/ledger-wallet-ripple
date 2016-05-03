package co.ledger.wallet.web.ethereum.controllers.onboarding

import co.ledger.wallet.web.ethereum.services.WindowService

/**
  * Created by pollas_p on 03/05/2016.
  */
trait OnBoardingController {
  val windowService: WindowService


  windowService.hideNavigationBar()
}
