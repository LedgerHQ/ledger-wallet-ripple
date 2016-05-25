package co.ledger.wallet.web.ethereum

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.RouteProvider
import co.ledger.wallet.web.ethereum.components._
import co.ledger.wallet.web.ethereum.controllers.WindowController
import co.ledger.wallet.web.ethereum.controllers.onboarding.{LaunchController, OpeningController}
import co.ledger.wallet.web.ethereum.controllers.wallet.{AccountController, ReceiveController, SendIndexController, SendPerformController}
import co.ledger.wallet.web.ethereum.core.utils.ChromePreferences
import co.ledger.wallet.web.ethereum.services.WindowService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    implicit val module = angular.createModule("app", Seq("ngRoute"))
    _module = module

    // Components
    NavigationBar.init(module)
    RefreshButton.init(module)
    LButton.init(module)
    ActionsBottomBar.init(module)
    QrCodeViewer.init(module)
    QrCodeScanner.init(module)
    ProgressBar.init(module)
    Spinner.init(module)
    SnackBar.init(module)

    // Controllers

    LaunchController.init(module)
    OpeningController.init(module)
    WindowController.init(module)
    AccountController.init(module)
    SendIndexController.init(module)
    SendPerformController.init(module)
    ReceiveController.init(module)

    // Services
    WindowService.init(module)

    module.config(initRoutes _)
    module.config(($compileProvider: js.Dynamic) => {
      $compileProvider.aHrefSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
      $compileProvider.imgSrcSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
    })
    module.run(initApp _)

    // Preferences tests
    ChromePreferences.load("toto", "toto") onComplete {
      case Success(_) =>
        val preferences = new ChromePreferences("Test")
        preferences.edit().putString("pref", "hey").commit()
        println(preferences.string("pref").get)
      case Failure(ex) => ex.printStackTrace()
    }
  }

  def initApp($http: HttpService, $rootScope: js.Dynamic, $location: js.Dynamic) = {
    $rootScope.location = $location
  }

  def initRoutes($routeProvider: RouteProvider) = {
    Routes.declare($routeProvider)
  }

  private var _module: RichModule = _
  def module = _module

}