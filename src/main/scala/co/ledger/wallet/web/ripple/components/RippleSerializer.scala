package co.ledger.wallet.web.ripple.components

import scala.scalajs.js

/**
  * Created by alix on 4/20/17.
  */

@js.native
trait RippleSerializer extends js.Object {
  def encode(json: js.Any): Array[Byte]
}

object RippleSerializer {
  private def instance = js.Dynamic.global.binary.asInstanceOf[RippleSerializer]
  def encode(json: String): Array[Byte] = instance.encode(json)
}