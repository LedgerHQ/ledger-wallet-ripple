package co.ledger.wallet.core.crypto

import co.ledger.wallet.core.utils.HexUtils

import scala.scalajs.js
/**
  * Created by alix on 4/7/17.
  */
object SHA256 {
  private val sjcl = js.Dynamic.global.sjcl

  def hash256(data: Array[Byte]): Array[Byte] = {
    val dataBit = sjcl.codec.hex.toBits(HexUtils.bytesToHex(data))
    val bitArray = sjcl.codec.hex.fromBits(sjcl.hash.sha256.hash(dataBit))
      .asInstanceOf[String]
    HexUtils.decodeHex(bitArray)
  }
}
