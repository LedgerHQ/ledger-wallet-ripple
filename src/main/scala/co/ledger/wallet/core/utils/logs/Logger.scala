package co.ledger.wallet.core.utils.logs

import java.io.{PrintStream, StringWriter}
import java.util.Date

import org.scalajs.dom.raw.{Blob, URL}

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
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
    log(level, Option(tag).getOrElse("Global"), entry.toString())
  }

  def log(level: String, tag: String, value: String) = {
    val date = new js.Date()
    js.Dynamic.global.console.log("%c" + LogExporter.formatLog(level, tag, value, date), s"color: ${levelToColor(level)}")
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

  private def levelToColor(level: String) = level match {
    case "D" => "#000000"
    case "I" => "#3fb34f"
    case "V" => "#999999"
    case "E" => "#ea2e49"
    case "WTF" => "#FF0000; font-weight: 800;"
  }

}

object Logger extends Logger {

  println(System.err)

}

object LogExporter {
  def toBlob: Future[Blob] = {
    val promise = Promise[Blob]()
    val writer = new StringWriter()
    LogEntry.readonly().cursor flatMap {(cursor) =>
      cursor foreach {(item) =>
        item match {
          case Some(entry) =>
            writer.append(formatLog(
              entry.level().orNull,
              entry.tag().orNull,
              entry.entry().orNull,
              entry.createdAt().orNull
            )).append("\n")
          case None =>
            val blob = new Blob(js.Array(writer.toString))
            promise.success(blob)
        }
      }
      promise.future
    }
  }

  def toUri: Future[URL] = {
    toBlob.map((blob) => js.Dynamic.global.URL.createObjectURL(blob).asInstanceOf[URL])
  }

  def download(): Future[Unit] = {
    toUri.map {(url: URL) =>
      val link = js.Dynamic.global.document.createElement("a");
      link.download = s"ledger_wallet_ethereum_${new Date().getTime}.logs"
      link.href = url
      link.click()
      ()
    }
  }

  def formatLog(level: String, tag: String, value: String, date: js.Date) = s"${date.toISOString()} $level/$tag: $value"

}