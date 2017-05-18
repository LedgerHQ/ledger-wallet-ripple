package co.ledger.wallet.web.ripple.controllers.onboarding

import autoupdater.Updater._
import autoupdater.{ApiUpdateRestClient, Updater}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{JQLite, Location}
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.core.device.ripple.LedgerApi
import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.web.ripple.components.WindowManager
import co.ledger.wallet.web.ripple.core.utils.{ChromeGlobalPreferences, ChromePreferences, JQueryHelper, OsHelper}
import co.ledger.wallet.web.ripple.services.{DeviceService, WindowService}
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers
import scala.util.{Failure, Success}

/**
  *
  * LaunchController
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 11/05/2016.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
class LaunchController(override val windowService: WindowService,
                       val deviceService: DeviceService,
                       $scope: Scope,
                       $element: JQLite,
                       $location: Location,
                       $route: js.Dynamic,
                       $routeParams: js.Dictionary[String])
  extends Controller with OnBoardingController {

  import LaunchController._

  import timers._

  println("launch path", js.JSON.stringify($routeParams))


  private var _scanRequest: ScanRequest = null
  private var _discover: Int = (if ($routeParams.contains("discover")) $routeParams("discover").toInt else 0)
  private val preferences = new ChromeGlobalPreferences("launch_screen")

  if (_discover == 1 || new ChromePreferences("update").int("skip").getOrElse(0) == 1) {
    _discover = 1
    new ChromePreferences("update").edit().putInt("skip", 1).commit()
  }

  private def animate(discover: Boolean) = {
    // Initialize default state
    println("animate(discover=", discover)
    JQueryHelper.injectCustomEasings()
    val header = jQuery($element.find("> header").asInstanceOf[JQLite](0))
    header.height(98)
    val section = jQuery($element.find("> section").asInstanceOf[JQLite](0))
    val document = jQuery(".onboarding-container")
    val plugFooter = jQuery($element.find("#plugFooter"))
    plugFooter.fadeOut(0)
    section.css("opacity", 0)
    val offset = header.offset().asInstanceOf[js.Dictionary[Double]]
    val translate = (document.outerHeight(true) / 2 - header.outerHeight(true) / 2) - document.css("padding-top").replace("px", "").toInt
    header.css("top", translate + "px")

    val duration = 750
    val easing = "default"
    setTimeout(OpeningAnimationDelay) {
      // Perform animation
      header.animate(js.Dictionary("top" -> 0), duration, easing)
      section.animate(js.Dictionary("opacity" -> 1), duration, easing)

      plugFooter.fadeIn(duration * 0.60)
      if (discover)
        startDeviceDiscovery()
    }
  }

  private def startDeviceDiscovery(): Unit = {
    _scanRequest = deviceService.requestScan()
    _scanRequest.onScanUpdate {
      case DeviceDiscovered(device) =>
        if (_scanRequest != null) {
          connectDevice(device)
          _scanRequest.stop()
          _scanRequest = null
        }
      case DeviceLost(device) =>
    }
    _scanRequest.duration = DeviceFactory.InfiniteScanDuration
    _scanRequest.start()
  }

  def connectDevice(device: Device): Unit = {
    println("connect device")
    device.connect() flatMap { (_) =>
      deviceService.registerDevice(device)
    } flatMap { (_) =>
      LedgerApi(device).walletIdentifier()
    } onComplete {
      case Success(wallet) =>
        incrementNumberOfConnection()
        if (true) {
          $location.url(s"/onboarding/opening")
          $route.reload()
        } else {
          $route.reload()
        }
      case Failure(ex) =>
        ex.printStackTrace()
        startDeviceDiscovery()
    }
  }

  def openHelpCenter(): Unit = {
    WindowManager.open("http://support.ledgerwallet.com/help_center")
  }

  def openLinuxInstruction(): Unit = {
    WindowManager.open("http://support.ledgerwallet.com/knowledge_base/topics/ledger-wallet-is-not-recognized-on-linux")
  }

  def numberOfConnection = new ChromeGlobalPreferences("launches").int("count").getOrElse(0)

  def incrementNumberOfConnection() = new ChromeGlobalPreferences("launches").edit().putInt("count", numberOfConnection + 1).commit()

  def settings() = {
    var default = new ChromeGlobalPreferences("Settings").string("node").getOrElse("wss://s1.ripple.com")
    if (default==null) default = "wss://s1.ripple.com"
    var input = js.Dynamic.global.prompt("Ripple Node :", default.asInstanceOf[String])
    if (!js.isUndefined(input)) {
      new ChromeGlobalPreferences("Settings").edit().putString("node", input.asInstanceOf[String]).commit()
    }
  }
  private def stopDeviceDiscovery(): Unit = {
    Option(_scanRequest) foreach { (request) =>
      request.stop()
    }
  }

  jQuery($element.find("#introFooter")).height(11)

  $scope.$on("$destroy", { (obj: js.Any, ob: js.Any) =>
    stopDeviceDiscovery()
  })


  {if (_discover == 1) {
    Future.successful(None)
  } else {
    println("checking version")
    Updater.isNewVersion()
  }} map { (isupdate) => {
      if (isupdate.isDefined) {
        $location.path(s"/onboarding/download/" ++ isupdate.get)
        $route.reload()
        false
      } else {
        true
      }
    }
  } andThen {
    case Success(false) => ()
    case everytimeexeptfalse => {
      println("start discovery")
      if (numberOfConnection == 0 && OsHelper.requiresUdevRules && $element.attr("controller-mode") != "linux") {
        if ($routeParams.contains("animated")) {
          animate(false)
        }
        $location.path("/onboarding/linux" + (if ($routeParams.contains("animated")) "/animated" else ""))
        $route.reload()
      } else {
        if ($routeParams.contains("animated")) {
          animate(true)
        } else {
          jQuery($element.find("#introFooter")).fadeOut(0)
          startDeviceDiscovery()
        }
      }
    }
  }
}

object LaunchController {
  val OpeningAnimationDelay = 1500
  def init(module: RichModule) = module.controllerOf[LaunchController]("LaunchController")
}