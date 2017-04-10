package co.ledger.wallet.core.crypto

import scala.scalajs.js
/**
  * Created by alix on 4/7/17.
  */
object SHA256 {
  private val sjcl = js.Dynamic.global.sjcl

  def hash256(data: Array[Byte]): Array[Byte] = {
    var value = ""
    for (byte <- data) {
      value += byte.toChar
    }
    val bitArray = sjcl.codec.hex.fromBits(sjcl.hash.sha256.hash(value))
      .asInstanceOf[String]
    ??? //HexUtils
  }
}
