package co.ledger.wallet.web.ripple.components

import co.ledger.wallet.core.utils.HexUtils

import scala.collection.mutable.ArrayBuffer
import scala.scalajs.js

/**
  * Created by alix on 4/20/17.
  */

@js.native
trait RippleSerializer extends js.Object {
  def encode(json: js.Any): String
}

object RippleSerializer {
  private def instance = js.Dynamic.global.binary.asInstanceOf[RippleSerializer]
  def encode(json: String): Array[Byte] = {
    HexUtils.decodeHex(instance.encode(js.JSON.parse(json)))
  }
}