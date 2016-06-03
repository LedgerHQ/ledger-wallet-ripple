package co.ledger.wallet.web.ethereum.i18n

import scala.scalajs.js

/**
  *
  * TranslateProvider
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
@js.native
trait TranslateProvider extends js.Any {
  def useStaticFilesLoader(options: js.Dictionary[String]): TranslateProvider = js.native
  def preferredLanguage(language: String): TranslateProvider = js.native
  def useSanitizeValueStrategy(strategy: String): TranslateProvider = js.native
  def determinePreferredLanguage(): TranslateProvider = js.native
  def fallbackLanguage(language: String): TranslateProvider = js.native
}
