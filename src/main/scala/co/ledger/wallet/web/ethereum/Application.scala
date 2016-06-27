package co.ledger.wallet.web.ethereum

import java.net.URI

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.RouteProvider
import co.ledger.wallet.core.utils.HexUtils
import co.ledger.wallet.core.utils.logs._
import co.ledger.wallet.core.wallet.ethereum.rlp.RLP
import co.ledger.wallet.web.ethereum.components._
import co.ledger.wallet.web.ethereum.controllers.WindowController
import co.ledger.wallet.web.ethereum.controllers.onboarding.{LaunchController, OpeningController}
import co.ledger.wallet.web.ethereum.controllers.wallet.{AccountController, ReceiveController, SendIndexController, SendPerformController}
import co.ledger.wallet.web.ethereum.core.net.JsWebSocketFactory
import co.ledger.wallet.web.ethereum.i18n.{I18n, TranslateProvider}
import co.ledger.wallet.web.ethereum.services.{DeviceService, SessionService, WindowService}

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.util.{Failure, Success}

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
    DeviceService.init(module)
    SessionService.init(module)

    js.Dynamic.global.chrome.storage.local.clear()

    module.config(initRoutes _)
    module.config(($compileProvider: js.Dynamic) => {
      $compileProvider.aHrefSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
      $compileProvider.imgSrcSanitizationWhitelist(js.RegExp("^\\s*(https?|ftp|mailto|file|chrome-extension):"))
    })
    module.config(initTranslate _)
    module.run(initApp _)

    LoggerPrintStream.init()
    LogSourceMapper.init()

    def test(data: Any) = {
      try
        println("Result > " + HexUtils.encodeHex(RLP.encode(data)))
      catch {
        case all: Throwable => all.printStackTrace()
      }
    }
    test("dog")
    test(Array("cat", "dog"))
    test("")
    test(Array[Unit]())
    test(15)
    test(1024)
    test("Lorem ipsum dolor sit amet, consectetur adipisicing elit")
    /*
    The string "dog" = [ 0x83, 'd', 'o', 'g' ]

The list [ "cat", "dog" ] = [ 0xc8, 0x83, 'c', 'a', 't', 0x83, 'd', 'o', 'g' ]

The empty string ('null') = [ 0x80 ]

The empty list = [ 0xc0 ]

The encoded integer 15 ('\x0f') = [ 0x0f ]

The encoded integer 1024 ('\x04\x00') = [ 0x82, 0x04, 0x00 ]
The set theoretical representation of two, [ [], [[]], [ [], [[]] ] ] = [ 0xc7, 0xc0, 0xc1, 0xc0, 0xc3, 0xc0, 0xc1, 0xc0 ]

The string "Lorem ipsum dolor sit amet, consectetur adipisicing elit" = [ 0xb8, 0x38, 'L', 'o', 'r', 'e', 'm', ' ', ... , 'e', 'l', 'i', 't' ]
     */
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