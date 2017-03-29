package co.ledger.wallet.web.ripple.controllers.onboarding

import co.ledger.wallet.web.ripple.services.WindowService

import scala.scalajs.js

/**
  * Created by pollas_p on 03/05/2016.
  */
trait OnBoardingController {
  val windowService: WindowService


  windowService.hideNavigationBar()

}
