package co.ledger.wallet.core.utils.logs

import co.ledger.wallet.web.ripple.core.database.{DatabaseDeclaration, Model, ModelCreator, QueryHelper}

/**
  *
  * LogEntry
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 10/06/2016.
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
class LogEntry extends Model("log_entry") {

  val id = int("id").autoincrement()
  val level = string("level")
  val tag = string("tag")
  val entry = string("entry").encrypted()
  val createdAt = date("createdAt").index()
  val owner = string("owner")

}

object LogEntry extends QueryHelper[LogEntry] with ModelCreator[LogEntry] {
  override def database: DatabaseDeclaration = LogsDatabaseDeclaration
  override def creator: ModelCreator[LogEntry] = this
  override def newInstance(): LogEntry = new LogEntry
}