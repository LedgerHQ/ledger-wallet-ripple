package co.ledger.wallet.core.utils.logs

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  *
  * Logger
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 13/05/2016.
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

class Logger {

  def d(log: js.Any*)(implicit LogTag: String = null, DisableLogging: Boolean = false) = {
    if (!DisableLogging) {
      append("D", LogTag, log)
    }
  }

  def i(log: js.Any*)(implicit LogTag: String = null, DisableLogging: Boolean = false) = {
    if (!DisableLogging) {
      append("I", LogTag, log)
    }
  }

  def v(log: js.Any*)(implicit LogTag: String = null, DisableLogging: Boolean = false) = {
    if (!DisableLogging) {
      append("V", LogTag, log)
    }
  }

  def e(log: js.Any*)(implicit LogTag: String = null, DisableLogging: Boolean = false) = {
    if (!DisableLogging) {
      append("E", LogTag, log)
    }
  }

  def wtf(log: js.Any*)(implicit LogTag: String = null, DisableLogging: Boolean = false) = {
    if (!DisableLogging) {
      append("WTF", LogTag, log)
    }
  }

  private def append(level: String, tag: String, values: Seq[js.Any]) = {
    val entry = new StringBuilder
    for (v <- values) {
      v match {
        case o: js.Object => entry.append(JSON.stringify(o))
        case other => entry.append(other.toString)
      }
      entry.append(" ")
    }
    put(level, Option(tag).getOrElse("Global"), entry.toString())
  }

  private def put(level: String, tag: String, value: String) = {
    val date = new js.Date()
    println(s"${date.toISOString()} $level/$tag: $value")
    val entry = new LogEntry
    entry.level.set(level)
    entry.tag.set(tag)
    entry.entry.set(value)
    entry.createdAt.set(date)

    LogEntry.readwrite().add(entry).commit().onComplete {
      case Success(_) =>
      case Failure(ex) => ex.printStackTrace()
    }
  }

}

object Logger extends Logger {

}