package co.ledger.wallet.web.ethereum.i18n

import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.util.{Failure, Success}

/**
  *
  * I18n
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 02/06/2016.
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
object I18n {

  def init(provider: TranslateProvider): Unit = {
    provider
      .useStaticFilesLoader(js.Dictionary(
        "prefix" -> "_locales/",
        "suffix" -> ".json"
      ))
      .useSanitizeValueStrategy("escape")
      .fallbackLanguage("en")
      .determinePreferredLanguage()
  }

  def loadManifest(): Future[I18nManifest] = {
    val promise = Promise[I18nManifest]()

    jQuery.ajax(js.Dynamic.literal(
      url = "_locales/manifest.json",
      success = { (data: String, textStatus: String, jqXHR: JQueryXHR) =>
        import upickle.default._
        promise.success(read[I18nManifest](data))
      },
      error = { (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) =>
        new Exception("No i18n manifest").printStackTrace()
      },
      `type` = "GET"
    ).asInstanceOf[JQueryAjaxSettings])

    promise.future andThen {
      case Success(manifest) => _manifest = manifest
      case Failure(ex) => ex.printStackTrace()
    }
  }

  def manifest = _manifest
  private var _manifest: I18nManifest = _

  case class I18nManifest(languages: Array[I18nLanguageEntry])

  case class I18nLanguageEntry(code: String, name: String, keys: String)

}
