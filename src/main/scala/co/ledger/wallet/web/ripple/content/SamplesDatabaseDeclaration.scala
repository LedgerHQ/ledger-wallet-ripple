package co.ledger.wallet.web.ripple.content

import co.ledger.wallet.web.ripple.core.database.{DatabaseDeclaration, ModelCreator, QueryHelper}
import co.ledger.wallet.web.ripple.core.idb.IndexedDb

import scala.concurrent.Future

/**
  *
  * SamplesDatabaseDeclaration
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 07/06/2016.
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
object SamplesDatabaseDeclaration extends DatabaseDeclaration {
  override def name: String = "samples"
  override def version: Int = 1
  override def models: Seq[QueryHelper[_]] = Seq(
    SampleModel
  )
}
