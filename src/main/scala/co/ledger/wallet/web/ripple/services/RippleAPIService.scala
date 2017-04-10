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
  val api = new RippleAPI

  def init(options: APIOption): Unit = {
    api.setOptions(options).onSuccess {
      case k => println("success fron future connection")
    println(k)}
  }

  def close(): Unit = {
    api.disconnect()
  }

  def preparePayment(parameters: api.PaymentParam): Future[api.PrepareResponse] = {
    api.execute("preparePayment", parameters)
      .map(decode[api.PrepareResponse](_).right.get)
  }

  def sign(parameters: api.SignParam): Future[api.SignedTransaction] = {
    api.execute("sign", parameters)
      .map(decode[api.SignedTransaction](_).right.get)
  }

  def submit(parameters: api.SubmitParam): Future[api.SubmittedTransaction] = {
    api.execute("submit", parameters)
      .map(decode[api.SubmittedTransaction](_).right.get)
  }
}

object RippleAPIService {
  def init(module: RichModule) = module.serviceOf[RippleAPIService]("rippleAPIService")

}