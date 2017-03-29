package co.ledger.wallet.core.net

import java.io.{ByteArrayInputStream, InputStream, StringWriter}

import org.json.{JSONArray, JSONObject}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
  *
  * HttpClient
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
trait HttpClient {
  implicit val ec: ExecutionContext
  var defaultReadTimeout = 30.seconds
  var defaultConnectTimeout = 10.seconds
  var retryNumber = 3
  var cacheResponses = true
  var followRedirect = true
  var defaultLogger: HttpRequestLogger

  private[this] val _defaultHttpHeaders = scala.collection.mutable.Map[String, String]()

  def setDefaultHttpHeader(headerField: (String, String)): Unit = {
    synchronized {
      _defaultHttpHeaders += headerField
    }
  }

  val baseUrl: String

  def get(path: String) = execute("GET", path)
  def put(path: String) = execute("PUT", path)
  def delete(path: String) = execute("DELETE", path)
  def post(path: String) = execute("POST", path)

  def execute(method: String, path: String): RequestBuilder = {
    val r = new RequestBuilder(method, baseUrl, path)
    configure(r)
    r
  }
  protected def configure(requestBuilder: RequestBuilder): Unit = {
    requestBuilder
      .readTimeout(defaultReadTimeout)
      .connectTimeout(defaultConnectTimeout)
      .retry(retryNumber)
      .cached(cacheResponses)
      .followRedirect(followRedirect)
      .logger(defaultLogger)
    _defaultHttpHeaders.foreach(requestBuilder.header(_))
  }
  protected val executor: HttpRequestExecutor
  private[this] def createResponseBuilder(request: HttpClient#Request): ResponseBuilder = {
    new ResponseBuilder(request)
  }

  class RequestBuilder(private val method: String,
                       private val baseUrl: String,
                       private val path: String) {

    def param(param: (String, Any)) = {
      _params += param
      this
    }

    def header(header: (String, Any)) = {
      _headers += header
      this
    }

    def retry(retry: Int) = {
      _retryNumber = retry
      this
    }

    def cached(enableCache: Boolean) = {
      _enableCache = enableCache
      this
    }

    def logger(logger: HttpRequestLogger) = {
      _logger = logger
      this
    }

    def success(code: Int) = {
      _successCodes += code
      this
    }

    def fail(code: Int) = {
      _failureCodes += code
      this
    }

    def followRedirect(followRedirect: Boolean) = {
      _followRedirect = followRedirect
      this
    }

    def contentType(contentType: String) = {
      _headers("Content-Type") = contentType
      this
    }

    def connectTimeout(timeout: Duration) = {
      _connectTimeout = timeout
      this
    }

    def readTimeout(timeout: Duration) = {
      _readTimeout = timeout
      this
    }

    def body(body: InputStream): this.type = {
      _body = body
      this
    }

    def body(json: JSONObject): this.type = {
      contentType("application/json")
      body(json.toString)
    }

    def body(string: String): this.type  = body(new ByteArrayInputStream(string.getBytes))
    def body(map: Map[String, Any]): this.type  = {
      val json = new JSONObject()
      map foreach {
        case (key, value) =>
          value match {
            case string: String => json.put(key, string)
            case int: Int => json.put(key, int)
            case int: Long => json.put(key, int)
            case int: Byte => json.put(key, int)
            case int: Short => json.put(key, int)
            case int: Boolean => json.put(key, int)
            case other => // Don't put
          }
      }
      body(json)
    }

    lazy val response: Future[HttpClient#Response] = {
      val request = toRequest
      val builder = createResponseBuilder(request)
      executor.execute(builder)
      _logger.onSendRequest(request)
      builder.future.map { (r) =>
        _logger.onRequestSucceed(r)
        _logger.onRequestCompleted(r)
        r
      } recover {
        case cause: HttpException =>
          _logger.onRequestFailed(cause.response, cause)
          _logger.onRequestCompleted(cause.response)
          throw cause
        case others => throw others
      }
    }

    import ResponseHelper._

    def json: Future[(JSONObject, HttpClient#Response)] = response.json
    def jsonArray: Future[(JSONArray, HttpClient#Response)] = response.jsonArray
    def string: Future[(String, HttpClient#Response)] = response.string
    def bytes: Future[(Array[Byte], HttpClient#Response)] = response.bytes
    def noResponseBody: Future[HttpClient#Response] = response.noResponseBody

    def toRequest = new Request(
      method,
      baseUrl,
      path,
      _headers.toMap,
      _params.toMap,
      _retryNumber,
      _connectTimeout,
      _readTimeout,
      _enableCache,
      _followRedirect,
      _logger,
      _body
    )

    private val _params = scala.collection.mutable.Map[String, Any]()
    private val _headers = scala.collection.mutable.Map[String, Any]()
    private val _successCodes = ArrayBuffer[Int]()
    private val _failureCodes = ArrayBuffer[Int]()
    private var _retryNumber = 0
    private var _enableCache = false
    private var _followRedirect = true
    private var _logger: HttpRequestLogger = _
    private var _connectTimeout: Duration = 5.seconds
    private var _readTimeout: Duration = 30.seconds
    private var _body: InputStream = _
  }

  class Request(val method: String,
                val baseUrl: String,
                val path: String,
                val headers: Map[String, Any],
                val params: Map[String, Any],
                val retryNumber: Int,
                val connectionTimeout: Duration,
                val readTimeout: Duration,
                val cached: Boolean,
                val followRedirect: Boolean,
                val logger: HttpRequestLogger,
                val body: InputStream
               ) {
    def url = {
      val writer = new StringWriter()
      if (baseUrl.last == '/') {
        writer.append(baseUrl.substring(0, baseUrl.length - 1))
      } else {
        writer.append(baseUrl)
      }
      writer.append(path)
      if (params.nonEmpty) {
        var first = true
        params foreach {
          case (key, value) =>
            if (first) {
              writer.append('?')
              first = false
            } else {
              writer.append('&')
            }
            writer.append(s"$key=$value")
        }
      }
      writer.toString
    }

    def bodyAsString = {
      if (body != null) {
        val b = new StringWriter()
        val buffer = new Array[Byte](4096)
        var read = 0
        body.reset()
        while ( {
          read = body.read(buffer); read
        } > 0) {
          b.append(new String(buffer, 0, read))
        }
        b.toString
      } else {
       ""
      }
    }

  }

  class Response(
                  val statusCode: Int,
                  val statusMessage: String,
                  val body: InputStream,
                  val headers: Map[String, String],
                  val bodyEncoding: String,
                  val request: HttpClient#Request) {


  }

  class ResponseBuilder(val request: HttpClient#Request) {
    private [this] val buildPromise = Promise[Response]()
    val future = buildPromise.future

    var statusCode: Int = 0
    var statusMessage: String = ""
    var body: InputStream = _
    var headers = Map[String, String]()
    var bodyEncoding = ""

    def failure(cause: Throwable) = {
      val response = toResponse
      buildPromise.failure(new HttpException(request, response, cause))
      response
    }

    def build(): Response = {
      val response = toResponse
      if ((200 <= response.statusCode && response.statusCode < 400) || response.statusCode == 304)
        buildPromise.success(response)
      else
        buildPromise.failure(new HttpException(request, toResponse, new Exception(s"$statusCode $statusMessage")))
      response
    }

    private[this] def toResponse =
      new Response(
        statusCode,
        statusMessage,
        body,
        headers,
        bodyEncoding,
        request
      )

  }

}
case class HttpException(request: HttpClient#Request, response:  HttpClient#Response, cause: Throwable) extends Exception
