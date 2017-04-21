package co.ledger.wallet.web.ripple.services

import scala.concurrent.ExecutionContext.Implicits.global
import biz.enef.angulate.Module.RichModule
import co.ledger.wallet.web.ripple.wallet.RippleLibApi
import biz.enef.angulate.Service

import scala.concurrent.Future

/**
  * Created by alix on 4/21/17.
  */
class RippleLibApiService () extends Service {
  val api = new RippleLibApi
  def init(options: api.APIOption): Future[Unit] = {
    api.setOptions(options) map(_ => ())
  }
}

object RippleLibApiService {
  def init(module: RichModule) = module.serviceOf[RippleLibApiService]("rippleLibApiService")
}