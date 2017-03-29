package co.ledger.wallet.core.net

/**
  *
  * ResponseHelper
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

import java.io.{ByteArrayOutputStream, StringWriter}
import java.nio.charset.Charset

import org.json.{JSONArray, JSONObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}

object ResponseHelper {

  implicit class ResponseFuture(f: Future[HttpClient#Response]) {

    def json: Future[(JSONObject, HttpClient#Response)] = {
      f.string.map { case (body, response) =>
        (new JSONObject(body), response)
      }
    }

    def jsonArray: Future[(JSONArray, HttpClient#Response)] = {
      f.string.map { case (body, response) =>
        (new JSONArray(body), response)
      }
    }

    def string: Future[(String, HttpClient#Response)] = {
      f.bytes.map { case (body, response) =>
        val writer = new StringWriter(body.length)
        body foreach {(char) =>
          writer.append(char.toChar)
        }
        (writer.toString, response)
      }
    }

    def bytes: Future[(Array[Byte], HttpClient#Response)] = {
      f.map { response =>
        val input = response.body
        val output = new ByteArrayOutputStream()
        val buffer = new Array[Byte](4096)
        var read = 0
        while ({read = input.read(buffer); read} > 0) {
          output.write(buffer, 0, read)
        }
        val result = output.toByteArray
        input.close()
        output.close()
        (result, response)
      }
    }

    def noResponseBody: Future[HttpClient#Response] = {
      f.andThen {
        case Success(response) =>
          response.body.close()
          response
        case Failure(cause) =>
          throw cause
      }
    }

  }

}