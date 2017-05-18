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
class DownloadController (override val windowService: WindowService,
                        deviceService: DeviceService,
                        $location: Location,
                        $route: js.Dynamic,
                        sessionService: SessionService,
                        $scope: Scope,
                        $element: JQLite,
                        $routeParams: js.Dictionary[String])
  extends Controller with OnBoardingController {

  val tag = $routeParams("tag")
  def accept(): Unit = {
    WindowManager.open("https://www.ledgerwallet.com/apps/ripple#get-the-apps")
  }

  def skip(): Unit = {
    $location.path(s"/onboarding/launch/1/")
  }

  def openHelpCenter(): Unit = {
    WindowManager.open("https://ledger.groovehq.com/knowledge_base/topics/ripple-classic-etc-important-notice")
  }

  def link(): Unit = {
    WindowManager.open("https://github.com/LedgerHQ/ledger-wallet-ripple/releases/tag/"+tag)

  } //https://github.com/LedgerHQ/ledger-wallet-ripple/releases/tag/0.0.3

}

object DownloadController {
  def init(module: RichModule) = module.controllerOf[DownloadController]("DownloadController")
}