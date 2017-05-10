package co.ledger.wallet.web.ripple.controllers.onboarding

import autoupdater.Updater
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.core.{JQLite, Location}
import co.ledger.wallet.web.ripple.components.WindowManager
import co.ledger.wallet.web.ripple.services.{DeviceService, SessionService, WindowService}

import scala.scalajs.js

/**
  * Created by alix on 5/9/17.
  */
class UpdateController (override val windowService: WindowService,
                        deviceService: DeviceService,
                        $location: Location,
                        $route: js.Dynamic,
                        sessionService: SessionService,
                        $scope: Scope,
                        $element: JQLite,
                        $routeParams: js.Dictionary[String])
  extends Controller with OnBoardingController {

  def accept(): Unit = {
    Updater.restartToUpdate()
  }

  def openHelpCenter(): Unit = {
    WindowManager.open("https://ledger.groovehq.com/knowledge_base/topics/ripple-classic-etc-important-notice")
  }
}

object UpdateController {
  def init(module: RichModule) = module.controllerOf[UpdateController]("UpdateController")
}