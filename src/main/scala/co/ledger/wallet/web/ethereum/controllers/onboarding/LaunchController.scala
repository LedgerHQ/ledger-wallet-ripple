package co.ledger.wallet.web.ethereum.controllers.onboarding

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{JQLite, Location}
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.core.device.ethereum.LedgerApi
import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.utils.DerivationPath
import co.ledger.wallet.web.ethereum.core.utils.{ChromeGlobalPreferences, ChromePreferences, JQueryHelper, OsHelper}
import co.ledger.wallet.web.ethereum.services.{DeviceService, WindowService}
import org.scalajs.jquery.jQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.timers
import scala.util.{Failure, Success}

/**
  *
  * LaunchController
  * ledger-wallet-ethereum-chrome
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
  private var _scanRequest: ScanRequest = null
  private val preferences = new ChromeGlobalPreferences("launch_screen")
  val chains = js.Dictionary(
    "ETH" -> "launch.eth",
    "ETC" -> "launch.etc"
  )
  var currentChain = preferences.string("chain").getOrElse("ETH")

  val onChainChanged: js.Function1[String, Unit] = {(chain: String) =>
    currentChain = chain
    preferences.edit().putString("chain", chain).commit()
  }

  private def initAnimation() = {

  }

  private def animate(discover: Boolean) = {
    // Initialize default state
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
    device.connect() flatMap {(_) =>
      deviceService.registerDevice(device)
    } flatMap {(_) =>
      LedgerApi(device).walletIdentifier()
    } flatMap {(_) =>
      SelectChainController.getRememberedChain(deviceService)
    } onComplete {
        case Success(chain) =>
          incrementNumberOfConnection()
          if (chain.isDefined) {
            $location.url(s"/onboarding/opening/${chain.get}/")
            $route.reload()
          } else {
            $location.url("/onboarding/chain/select")
            $route.reload()
          }
        case Failure(ex) =>
          ex.printStackTrace()
          startDeviceDiscovery()
      }
  }

  def openHelpCenter(): Unit = {
    js.Dynamic.global.open("http://support.ledgerwallet.com/help_center")
  }

  def openLinuxInstruction(): Unit = {
    js.Dynamic.global.open("http://support.ledgerwallet.com/knowledge_base/topics/ledger-wallet-is-not-recognized-on-linux")
  }

  def numberOfConnection = new ChromeGlobalPreferences("launches").int("count").getOrElse(0)
  def incrementNumberOfConnection() = new ChromeGlobalPreferences("launches").edit().putInt("count", numberOfConnection + 1).commit()

  private def stopDeviceDiscovery(): Unit = {
    Option(_scanRequest) foreach {(request) =>
      request.stop()
    }
  }

  jQuery($element.find("#introFooter")).height(11)

  $scope.$on("$destroy", {(obj: js.Any, ob: js.Any) =>
    stopDeviceDiscovery()
  })

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

object LaunchController {
  val OpeningAnimationDelay = 1500
  def init(module: RichModule) = module.controllerOf[LaunchController]("LaunchController")
}