package co.ledger.wallet.web.ripple.core.sjcl

import scala.scalajs.js

/**
  *
  * SjclAesCipher
  * ledger-wallet-ripple-chrome
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
class SjclAesCipher(key: String, iv: String = "554f0cafd67ddcaa", salt: String = "846cea3ae6a33474d6ae2221d8563eaaba73ef9ea20e1803") {
  private val sjcl = js.Dynamic.global.sjcl

  def encrypt(data: String): String = {
    val encryption = sjcl.json._encrypt(key, data, params)
    sjcl.codec.base64.fromBits(encryption.ct, 0).asInstanceOf[String]
  }

  def decrypt(data: String): String = {
    val p = params
    p.ct = sjcl.codec.base64.toBits(data)
    sjcl.json._decrypt(key, p).asInstanceOf[String]
  }

  private def params = js.Dynamic.literal(
    v = 1,
    iter = 1000,
    ks = 256,
    ts = 128,
    mode = "ccm",
    adata = "",
    cipher = "aes",
    iv = sjcl.codec.base64.toBits(iv),
    salt = sjcl.codec.base64.toBits(salt)
  )
}