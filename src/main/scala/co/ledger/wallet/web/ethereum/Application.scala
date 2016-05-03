package co.ledger.wallet.web.ethereum

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import biz.enef.angulate.ext.{Route, RouteProvider}
import co.ledger.wallet.web.ethereum.controllers.{TestController, WindowController}
import co.ledger.wallet.web.ethereum.services.WindowService

import scala.scalajs.js.JSApp

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    implicit val module = angular.createModule("app", Seq("ngRoute"))
    _module = module

    // Components


    // Controllers
    WindowController.init(module)
    TestController.init(module)

    // Services
    WindowService.init(module)

    module.config(initRoutes _)
    module.run(initApp _)
    val toto: Directive = null
    println("Hello World!")
    println(classOf[TestController].getName)
  }

  def initApp($http: HttpService) = {
    println("App initialized")
  }

  def initRoutes($routeProvider: RouteProvider) = {
    $routeProvider
      .when("/toto", Route(templateUrl = "/templates/toto.html" ))
      .otherwise( Route( redirectTo = "/" ) )
  }

  private var _module: RichModule = _
  def module = _module.self

}