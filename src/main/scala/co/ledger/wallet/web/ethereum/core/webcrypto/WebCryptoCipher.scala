package co.ledger.wallet.web.ethereum.core.webcrypto

import java.nio.charset.Charset

import org.scalajs.dom.crypto.{GlobalCrypto, HashAlgorithm}

import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.typedarray.{ArrayBuffer, Uint8Array}
import scala.concurrent.ExecutionContext.Implicits.global
/**
  *
  * WebCryptoCipher
  * ledger-wallet-ethereum-chrome
  *
  * Created by Pierre Pollastri on 11/07/2016.
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
class WebCryptoCipher(key: String, iv: Array[Byte], digestAlgorithm: String, algorithm: String, encoding: String) {
  import GlobalCrypto.crypto
  import org.scalajs.dom.crypto._
  import scala.scalajs.js.Thenable.Implicits._

  private val digestAlgo =  js.Dynamic.literal(name = digestAlgorithm).asInstanceOf[HashAlgorithm]
  private val algorithmIdentifier = js.Dynamic.literal(name = algorithm, iv = new Uint8Array(js.Array(iv:_*))).asInstanceOf[AlgorithmIdentifier]
  private lazy val _webcryptoKey = {
    val k = js.Array[Byte](key.getBytes(Charset.forName(encoding)):_*)
    val buffer = new Uint8Array(k)
    crypto.subtle.digest(digestAlgorithm, buffer).toFuture flatMap {(key) =>
      crypto.subtle.importKey(KeyFormat.raw, key.asInstanceOf[BufferSource], algorithm.asInstanceOf[KeyAlgorithmIdentifier], true, js.Array(KeyUsage.encrypt, KeyUsage.decrypt)).toFuture
    } map {(key) =>
      key.asInstanceOf[CryptoKey]
    }
  }

  def encrypt(data: Array[Byte]): Future[Array[Byte]] = {
    _webcryptoKey flatMap {(key) =>
      crypto.subtle.encrypt(algorithmIdentifier, key, new Uint8Array(js.Array(data:_*))).toFuture
    } map {(data) =>
      val view = new Uint8Array(data.asInstanceOf[ArrayBuffer])
      val result = new scala.collection.mutable.ArrayBuffer[Byte](view.length)
      for (i <- 0 until view.length) {
        result += view(i).toByte
      }
      result.toArray
    }
  }

  def decrypt(data: Array[Byte]): Future[Array[Byte]] = decrypt(new Uint8Array(js.Array(data:_*)))

  def decrypt(data: Uint8Array): Future[Array[Byte]] = {
    _webcryptoKey flatMap {(key) =>
      crypto.subtle.decrypt(algorithmIdentifier, key, data).toFuture
    } map {(result) =>
      val view = new Uint8Array(result.asInstanceOf[ArrayBuffer])
      val out = new scala.collection.mutable.ArrayBuffer[Byte](view.length)
      for (i <- 0 until view.length) {
        out += view(i).toByte
      }
      out.toArray
    }
  }
}

object WebCryptoCipher {

  def AESCBC256(key: String, iv: Array[Byte] = Array.fill[Byte](16)(0)): WebCryptoCipher = {
    new WebCryptoCipher(key, iv, "SHA-256", "AES-CBC", "utf-8")
  }

}

/*
  /*
  class Cipher

  constructor: (key, {algorithm, encoding} = {}) ->
    @_keyPromise = null
    @_encoding = encoding or 'utf-8'
    @_algorithm = algorithm or 'AES-CBC'
    @_key = key
    @_encoder = new TextEncoder(@_encoding)
    @_decoder = new TextDecoder(@_encoding)

  encrypt: (data) ->
    data = @_encode(data)
    @_importKey().then (key) =>
      crypto.subtle.encrypt(name: @_algorithm, iv: @_iv(), key, data)

  decrypt: (data) ->
    @_importKey().then (key) =>
      crypto.subtle.decrypt(name: @_algorithm, iv: @_iv(), key, data)
    .then (data) =>
      @_decode(data)

  _encode: (data) -> @_encoder.encode(data).buffer

  _decode: (data) -> @_decoder.decode(data)

  _importKey: ->
    @_keyPromise ||=
      Q(crypto.subtle.digest(name: 'SHA-256', @_encode(@_key))).then (key) =>
        crypto.subtle.importKey("raw", key, name: @_algorithm, true, ['encrypt', 'decrypt'])

  _iv: -> @__iv ||= new Uint8Array(16)

   */
 */