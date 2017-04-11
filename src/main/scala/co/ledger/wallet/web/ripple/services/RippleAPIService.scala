package co.ledger.wallet.web.ripple.services

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.Service
import io.circe.parser.decode
import org.ripple.api.{APIOption, RippleAPI}
import org.ripple.api.RippleAPIObject

import scala.concurrent.{ExecutionContext, Future}
import concurrent.ExecutionContext.Implicits.global
/**
  * Created by alix on 4/5/17.
  */
class RippleAPIService () extends Service {
  val api = new RippleAPI   //renvoie future
// val option[future ripple]
  def init(options: APIOption): Unit = {
    api.setOptions(options).onSuccess {
      case k => println("success fron future connection")
    println(k)}
  }

  def close(): Unit = {
    api.disconnect()
  }

}

object RippleAPIService {
  def init(module: RichModule) = module.serviceOf[RippleAPIService]("rippleAPIService")

}