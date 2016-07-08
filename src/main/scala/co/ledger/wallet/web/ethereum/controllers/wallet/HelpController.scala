package co.ledger.wallet.web.ethereum.controllers.wallet

import java.util.Date

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.{Controller, Scope}
import co.ledger.wallet.core.utils.logs.{LogExporter, Logger}
import co.ledger.wallet.web.ethereum.core.filesystem.ChromeFileSystem
import co.ledger.wallet.web.ethereum.core.utils.PermissionsHelper
import co.ledger.wallet.web.ethereum.services.{SessionService, WindowService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  *
  * HelpController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 07/07/2016.
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
class HelpController(override val windowService: WindowService,
                    $scope: Scope,
                     sessionService: SessionService
                    ) extends Controller with WalletController {


  def browseKnowledgeBase(): Unit = {
    js.Dynamic.global.open("http://support.ledgerwallet.com/help_center")
  }

  def contactSupport(): Unit = {
    js.Dynamic.global.open("mailto:hello@ledger.fr")
  }

  def exportLogs(): Unit = {
    println("Exporting")
    PermissionsHelper.requestIfNecessary("fileSystem.write") flatMap {_ =>
      ChromeFileSystem.chooseFileEntry(s"ledger-ethereum-chrome-logs-${new Date().getTime}.logs")
    } flatMap {(entry) =>
      LogExporter.toBlob flatMap {(content) =>
        entry.write(content)
      }
    }
  }


}

object HelpController {
  def init(module: RichModule) = module.controllerOf[HelpController]("HelpController")
}