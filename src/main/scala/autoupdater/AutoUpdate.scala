package autoupdater

import co.ledger.wallet.web.ripple.core.net.JQHttpClient
import co.ledger.wallet.web.ripple.core.utils.ChromePreferences

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js

/**
  * Created by alix on 5/4/17.
  */

@js.native
trait AutoUpdate extends js.Object {

  def readRemoteManifest(): scalajs.js.Promise[js.Dictionary[js.Any]]

  def checkNewVersion(rManifest: js.Dictionary[js.Any]): scalajs.js.Promise[Boolean]

  def download(srcUri: String, targetDir: String, onProgress: js.Function): scalajs.js.Promise[String] //TODO: optionnal debounce time

  def unpack(updateFile: String): scalajs.js.Promise[String]

  def restartToSwap(): scalajs.js.Promise[js.Any]

  var manifest: js.Dictionary[js.Any] = js.native

}

object AutoUpdate {
  def apply() =  js.Dynamic.newInstance(js.Dynamic.global.nwAutoupdate)(js.Dynamic.global.chrome.runtime.getManifest(),
    js.Dynamic.literal(strategy = "ScriptSwap")).asInstanceOf[AutoUpdate] //TODO : add app Path
}

object Updater {

  val autoUpdate: AutoUpdate = AutoUpdate()

  val httpClient = new JQHttpClient(autoUpdate.manifest("manifestUrl").asInstanceOf[String])

  var os = js.Dynamic.global.os.`type`().asInstanceOf[String] match {
    case "Linux" => "linux_deb"
    case "Darwin" => "osx"
    case "Windows_NT" => "windows"
  }

  if (js.Dynamic.global.os.arch().asInstanceOf[String] == "x64") {
    os ++ "_64"
  }

  def versionCompare(str1: String, str2: String) = {
    val vals1 = str1.split("\\.")
    val vals2 = str2.split("\\.")
    var i = 0
    // set index to first non-equal ordinal or length of shortest version string
    while ( {
      i < vals1.length && i < vals2.length && vals1(i) == vals2(i)
    }) i += 1
    // compare first non-equal ordinal number
    if (i < vals1.length && i < vals2.length) {
      val diff = Integer.valueOf(vals1(i)).compareTo(Integer.valueOf(vals2(i)))
      Integer.signum(diff)
    }
    // the strings are equal or one string is a substring of the other
    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
    Integer.signum(vals1.length - vals2.length)
  }

  var newVersion = ""

  def isNewVersion(): Future[Boolean] = {
    var api = new ApiUpdateRestClient(httpClient)
    api.lastVersion(os) map {(latest) =>
      newVersion = latest
      versionCompare(latest,autoUpdate.manifest("version").asInstanceOf[String]) > 0
    }
  }

  def download(): Future[String] = {
    autoUpdate.download(autoUpdate.manifest("manifestUrl").asInstanceOf[String]
      ++ autoUpdate.manifest("version").asInstanceOf[String] ++ os,
      js.Dynamic.global.UPDATE_DIR.asInstanceOf[String],
      {() => ()}).toFuture
  }

  def updateProcess(): Future[Unit] = {
    println("starting updating process")
    if (new ChromePreferences("update").boolean("readyToUpdate").getOrElse(false)) {
      println("flag found")

      new ChromePreferences("update").edit().putBoolean("readyToUpdate", false)
      autoUpdate.restartToSwap().toFuture map {(_) => ()}
    } else {
      println("checking new updates")

      isNewVersion().flatMap({ (test) =>
        if (test) {
          println("downloading")

          download() flatMap { (updateFile) =>
            autoUpdate.unpack(updateFile).toFuture map { (updateDir) =>
              println("setting flag")

              new ChromePreferences("update").edit().putBoolean("readyToUpdate", true)
                //.putString("version", newVersion)
                //.putString("updateDir", updateDir)
                .commit()
            }
          }
        } else {
          Future.successful()
        }
      })
    }
  }
}

