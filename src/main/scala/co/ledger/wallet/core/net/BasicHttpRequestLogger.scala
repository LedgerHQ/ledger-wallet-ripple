package co.ledger.wallet.core.net

/**
  *
  * BasicHttpLogger
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 15/06/2016.
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
import co.ledger.wallet.core.utils.logs.Logger

class BasicHttpRequestLogger extends HttpRequestLogger {

  implicit val LogTag = "HTTP"
  implicit val DisableLogging = false

  override def onSendRequest(request: HttpClient#Request): Unit =
    Logger.d(s"[${request.method}] ${request.url.toString}")

  override def onRequestFailed(response: HttpClient#Response, cause: Throwable): Unit = {}

  override def onRequestSucceed(response: HttpClient#Response): Unit = {}

  override def onRequestCompleted(response: HttpClient#Response): Unit = {
    Logger.d(s"[${response.request.method}] ${response.request.url.toString} - ${response.statusCode} ${response.statusMessage}")
  }
}