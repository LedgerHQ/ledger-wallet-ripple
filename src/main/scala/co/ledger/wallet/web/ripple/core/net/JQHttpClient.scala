package co.ledger.wallet.web.ripple.core.net

import java.io.ByteArrayInputStream

import co.ledger.wallet.core.net.{BasicHttpRequestLogger, HttpClient, HttpRequestExecutor, HttpRequestLogger}
import co.ledger.wallet.web.ripple.core.utils.JQueryHelper
import org.scalajs.jquery.JQueryXHR

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  *
  * JQHttpClient
  * ledger-wallet-ripple-chrome
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
class JQHttpClient(override val baseUrl: String) extends HttpClient {
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  override protected val executor: HttpRequestExecutor = new HttpRequestExecutor {
    override def execute(responseBuilder: co.ledger.wallet.core.net.HttpClient#ResponseBuilder): Unit = {
      val request = responseBuilder.request
      val headers = js.Dictionary[js.Any]()
      request.headers foreach {
        case (key, value) =>
          headers(key) = value.toString
      }
      try {
        JQueryHelper.$.ajax(js.Dictionary[js.Any](
          "method" -> request.method,
          "url" -> request.url,
          "headers" -> headers,
          "timeout" -> (request.readTimeout.toMillis + request.connectionTimeout.toMillis),
          "data" -> request.bodyAsString,
          "complete" -> { (xhr: JQueryXHR, status: String) =>
            responseBuilder.statusCode = xhr.status
            responseBuilder.statusMessage = xhr.statusText
            responseBuilder.bodyEncoding = "utf-8"
            if (xhr.status != 0)
              responseBuilder.body = new ByteArrayInputStream(xhr.responseText.getBytes)
            responseBuilder.build()
          }
        ))
      } catch {
        case er: Throwable =>
          responseBuilder.failure(er)
      }
      request.body.close()
    }
  }
  override var defaultLogger: HttpRequestLogger = new BasicHttpRequestLogger
}

object JQHttpClient {
  val etcInstance = new JQHttpClient("https://api.ledgerwallet.com/blockchain/v2/ethc")
  val ethInstance = new JQHttpClient("https://api.ledgerwallet.com/blockchain/v2/eth")
}