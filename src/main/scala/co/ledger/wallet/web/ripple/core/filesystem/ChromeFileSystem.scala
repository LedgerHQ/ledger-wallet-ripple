package co.ledger.wallet.web.ripple.core.filesystem

import org.scalajs.dom.raw.Blob

import scala.concurrent.{Future, Promise}
import scala.scalajs.js

/**
  *
  * ChromeFileSystem
  * ledger-wallet-ripple-chrome
  *
  * Created by Pierre Pollastri on 08/07/2016.
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
class ChromeFileSystem {

  def chooseFileEntry(suggestedName: String, acceptMultiple: Boolean = false): Future[FileEntry] = {
    // chrome.fileSystem.chooseEntry(object options, function callback)
    val promise = Promise[FileEntry]()
    js.Dynamic.global.chrome.fileSystem.chooseEntry(js.Dictionary(
      "type" -> "saveFile",
      "suggestedName" -> suggestedName
    ), {(entry: js.Dynamic) =>
      if (js.isUndefined(entry)) {
        promise.failure(new Exception(js.Dynamic.global.chrome.runtime.lastError.message.toString()))
      } else {
        promise.success(new FileEntry(entry))
      }
    })
    promise.future
  }

  class FileEntry(entry: js.Dynamic) {

    def write(blob: Blob): Future[Unit] = {
      val promise = Promise[Unit]()
      var truncated = false
      entry.createWriter({(writer: js.Dynamic) =>
        writer.onwriteend = {() =>
          truncated = true
          // You need to explicitly set the file size to truncate
          // any content that might have been there before
          writer.truncate(blob.size)
          promise.success()
        }
        writer.onerror = {(e: js.Dynamic) =>
          promise.failure(new Exception(e.toString()))
        }
        writer.write(blob)
      })
      /*
       fileEntry.createWriter(function(fileWriter) {

      var truncated = false;
      var blob = new Blob([contents]);

      fileWriter.onwriteend = function(e) {
        if (!truncated) {
          truncated = true;
          // You need to explicitly set the file size to truncate
          // any content that might have been there before
          this.truncate(blob.size);
          return;
        }
        status.innerText = 'Export to '+fileDisplayPath+' completed';
      };

      fileWriter.onerror = function(e) {
        status.innerText = 'Export failed: '+e.toString();
      };

      fileWriter.write(blob);

    });
       */
      promise.future
    }

  }

}

object ChromeFileSystem extends ChromeFileSystem