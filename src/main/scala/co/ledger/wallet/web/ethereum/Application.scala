package co.ledger.wallet.web.ethereum

import biz.enef.angulate._
import biz.enef.angulate.core.HttpService
import co.ledger.wallet.web.ethereum.controllers.WindowController

import scala.scalajs.js.JSApp

/**
  * Created by pollas_p on 28/04/2016.
  */

object Application extends JSApp{

  @scala.scalajs.js.annotation.JSExport
  override def main(): Unit = {
    implicit val module = angular.createModule("app")

    // Components


    // Controllers
    WindowController.init()

    module.run(initApp _)
    println("Hello World!")
  }

  def initApp($http: HttpService) = {
    println("App initialized")
  }

}