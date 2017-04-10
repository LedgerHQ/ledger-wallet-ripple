package co.ledger.wallet.web.ripple

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.RouteProvider
import co.ledger.wallet.core.utils.logs._
import co.ledger.wallet.web.ripple.components._
import co.ledger.wallet.web.ripple.controllers.WindowController
import co.ledger.wallet.web.ripple.controllers.onboarding.{LaunchController, OpeningController, SplitDisclaimerController}
import co.ledger.wallet.web.ripple.controllers.wallet._
import co.ledger.wallet.web.ripple.core.utils.ChromePreferences
import co.ledger.wallet.web.ripple.filters.DateFormatFilter
import co.ledger.wallet.web.ripple.i18n.{I18n, TranslateProvider}
import co.ledger.wallet.web.ripple.services.{DeviceService, RippleAPIService, SessionService, WindowService}
import org.ripple.api.RippleAPI

import scala.scalajs.js
import scala.scalajs.js.JSApp

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    run()
  }

  def run(): Unit = {
    implicit val module = angular.createModule("app", Seq("ngRoute", "pascalprecht.translate"))
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
    AmountLabel.init(module)
    Selector.init(module)

    // Controllers
    LaunchController.init(module)
    OpeningController.init(module)
    WindowController.init(module)
    AccountController.init(module)
    SendIndexController.init(module)
    SendPerformController.init(module)
    ReceiveController.init(module)
    HelpController.init(module)
    SplitDisclaimerController.init(module)

    // Services
    WindowService.init(module)
    DeviceService.init(module)
    RippleAPIService.init(module)
    SessionService.init(module)

    // Filters
    DateFormatFilter.init(module)


    ChromePreferences.init()
    module.config(initRoutes _)
    module.config(($compileProvider: js.Dynamic) => {
      $compileProvider.aHrefSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
      $compileProvider.imgSrcSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
    })
    module.config(initTranslate _)
    module.run(initApp _)
    LoggerPrintStream.init()
    LogSourceMapper.init()

  }

  def initApp($http: HttpService, $rootScope: js.Dynamic, $location: js.Dynamic) = {
    $rootScope.location = $location
  }

  def initRoutes($routeProvider: RouteProvider) = {
    Routes.declare($routeProvider)
  }

  def initTranslate($translateProvider: TranslateProvider) = {
    I18n.init($translateProvider)
  }

  private var _module: RichModule = _
  def module = _module

}