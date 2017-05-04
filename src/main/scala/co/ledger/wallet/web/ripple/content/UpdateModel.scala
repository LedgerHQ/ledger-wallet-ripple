package co.ledger.wallet.web.ripple.content

import co.ledger.wallet.web.ripple.core.database.Model

/**
  * Created by alix on 5/4/17.
  */
class UpdateModel extends Model("transactions") {
  val version = string("version").unique().index()
  val updateDir = string("updateDir")
  val downloadedAt = date("downloadedAt").index()
  val installed = boolean("installed")
}
