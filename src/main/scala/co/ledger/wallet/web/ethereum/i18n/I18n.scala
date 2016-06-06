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
    val allKeys = js.Array[String]()
    val aliases = js.Dictionary[String]()
    for (language <- I18nLanguagesManifest.languages) {
      allKeys.push(language.code)
      aliases(language.keys) = language.code
    }
    provider
      .useStaticFilesLoader(js.Dictionary(
        "prefix" -> "_locales/",
        "suffix" -> ".json"
      ))
      .useSanitizeValueStrategy("escape")
      .fallbackLanguage("en")
      .registerAvailableLanguageKeys(allKeys, aliases)
      .determinePreferredLanguage()
  }

}
