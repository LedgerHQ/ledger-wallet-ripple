package co.ledger.wallet.web.ethereum.controllers.onboarding

import biz.enef.angulate.{Controller, Scope}
import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.{JQLite, Location}
import biz.enef.angulate.ext.Route
import co.ledger.wallet.core.device.{Device, DeviceFactory}
import co.ledger.wallet.core.device.DeviceFactory.{DeviceDiscovered, DeviceLost, ScanRequest}
import co.ledger.wallet.web.ethereum.core.utils.JQueryHelper
import co.ledger.wallet.web.ethereum.services.{DeviceService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.scalajs.js.timers
import org.scalajs.jquery.jQuery

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

  val test = toto
  toto += 1
  println(s"Create $test")

  private def animate() = {
    // Initialize default state
    JQueryHelper.injectCustomEasings()
    val header = jQuery($element.find("> header").asInstanceOf[JQLite](0))
    header.height(100)
    val section = jQuery($element.find("> section").asInstanceOf[JQLite](0))
    val document = jQuery("body")
    val introFooter = jQuery($element.find("#introFooter"))
    val plugFooter = jQuery($element.find("#plugFooter"))
    plugFooter.fadeOut(0)
    section.css("opacity", 0)
    val offset = header.offset().asInstanceOf[js.Dictionary[Double]]
    js.Dynamic.global.console.log(offset)
    val translate = (document.outerHeight(true) / 2 - header.outerHeight(true) / 2) - offset("top").toInt
      + (section.css("margin-top").replace("px", "").toInt / 2)
    header.css("top", translate + "px")

    val duration = 750
    val easing = "default"
    setTimeout(OpeningAnimationDelay) {
      // Perform animation
      header.animate(js.Dictionary("top" -> 0), duration, easing)
      section.animate(js.Dictionary("opacity" -> 1), duration, easing)

      introFooter.fadeOut(duration * 0.60)
      plugFooter.fadeIn(duration * 0.60)

      startDeviceDiscovery()
    }
  }

  private def startDeviceDiscovery(): Unit = {
    println(s"START DISCOVERY $test")
    _scanRequest = deviceService.requestScan()
    println(_scanRequest)
    _scanRequest.onScanUpdate {
      case DeviceDiscovered(device) =>
        println(s"Discovered $test " + device)
        if (_scanRequest != null) {
          connectDevice(device)
          _scanRequest.stop()
          _scanRequest = null
        }
      case DeviceLost(device) =>
        println(s"Lost $test " + device)
    }
    _scanRequest.duration = DeviceFactory.InfiniteScanDuration
    _scanRequest.start()
  }

  def connectDevice(device: Device): Unit = {
    device.connect() flatMap {(_) =>
      deviceService.registerDevice(device)
    } onComplete {
      case Success(uuid) =>
        $location.url("/onboarding/opening/")
        $route.reload()
      case Failure(ex) =>
        ex.printStackTrace()
        startDeviceDiscovery()
    }
  }

  private def stopDeviceDiscovery(): Unit = {
    println(s"STOP DISCOVERY $test")
    println(_scanRequest)
    Option(_scanRequest) foreach {(request) =>
      println("SCAN STOP")
      request.stop()
    }
  }

  jQuery($element.find("#introFooter")).height(11)

  if ($routeParams.contains("animated")) {
    animate()
  } else {
    jQuery($element.find("#introFooter")).fadeOut(0)
    startDeviceDiscovery()
  }

  $scope.$on("$destroy", {(obj: js.Any, ob: js.Any) =>
    js.Dynamic.global.console.log(obj, ob)
    stopDeviceDiscovery()
  })
}

object LaunchController {
  val OpeningAnimationDelay = 1500
  var toto = 0
  def init(module: RichModule) = module.controllerOf[LaunchController]("LaunchController")
}