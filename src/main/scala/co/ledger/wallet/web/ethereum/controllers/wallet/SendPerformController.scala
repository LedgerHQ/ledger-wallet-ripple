package co.ledger.wallet.web.ethereum.controllers.wallet

import biz.enef.angulate.Controller
import biz.enef.angulate.Module.RichModule

/**
  *
  * SendPerformViewController
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 10/05/2016.
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
class SendPerformController extends Controller {
  import SendPerformController._

  def isInProgressionMode = _currentMode == ProgressionMode
  def isInWaitingMode = _currentMode == WaitingMode

  def switchMode() = {
    if (isInProgressionMode)
      _currentMode = WaitingMode
    else
      _currentMode = ProgressionMode
  }

  private var _currentMode = ProgressionMode
}

object SendPerformController {

  val ProgressionMode = 0
  val WaitingMode = 1

  def init(module: RichModule) = module.controllerOf[SendPerformController]("SendPerformController")

}