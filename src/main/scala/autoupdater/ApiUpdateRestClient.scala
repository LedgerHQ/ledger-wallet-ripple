package autoupdater

import co.ledger.wallet.core.net.HttpClient
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by alix on 5/4/17.
  */
class ApiUpdateRestClient(http: HttpClient) {

  def lastVersion(os: String): Future[String] = {/*
    val request = http.get(s"/api/resolve?platform=$os&channel=stable")
    request.json.map({
      case (json, _) =>
        json.getString("tag")
    })*/
    Future.successful("0.0.2")
  }
}
