package co.ledger.wallet.web.ripple.controllers.wallet

import biz.enef.angulate.Scope
import co.ledger.wallet.web.ripple.services.{RippleAPIService, SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by pollas_p on 03/05/2016.
  */
trait WalletController {
  val sessionService: SessionService
  val windowService: WindowService
  val rippleAPIService: RippleAPIService
  val $scope: Scope
  implicit val ws = windowService
  implicit val api = rippleAPIService

  windowService.showNavigationBar()

  windowService.onRefreshClicked {() =>
    sessionService.currentSession.get.wallet.synchronize()
  }

  sessionService.currentSession.get.wallet.isSynchronizing() foreach {(isSynchronizing) =>
    if (isSynchronizing)
      windowService.notifyRefresh()
  }
}
