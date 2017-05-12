package autoupdater

import co.ledger.wallet.web.ripple.core.net.JQHttpClient
import co.ledger.wallet.web.ripple.core.utils.{ChromeGlobalPreferences, ChromePreferences}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.timers._

/**
  * Created by alix on 5/4/17.
  */

@js.native
trait AutoUpdate extends js.Object {

  def readRemoteManifest(): scalajs.js.Promise[js.Dictionary[js.Any]]

  def checkNewVersion(rManifest: js.Dictionary[js.Any]): scalajs.js.Promise[Boolean]

  def unpack(updateFile: String): scalajs.js.Promise[String]

  def restartToSwap(): scalajs.js.Promise[js.Any]

  var manifest: js.Dictionary[js.Any] = js.native

}

object AutoUpdate {
  def apply(dir: String) =  {
    js.Dynamic.global.nwAutoupdaterFactory(js.Dynamic.literal(strategy = "ScriptSwap",
        updateDir = dir, logPath = "/ledger.log")).asInstanceOf[AutoUpdate]
  }
}

object Updater {

  private var _updateDir = "/tmp"
  private val _downloadDir = "/tmp"
  val autoUpdate: AutoUpdate = AutoUpdate(_updateDir)
  val httpClient = new JQHttpClient(autoUpdate.manifest("manifestUrl").asInstanceOf[String])

  var os = js.Dynamic.global.os.`type`().asInstanceOf[String] match {
    case "Linux" => "linux"
    case "Darwin" => "osx"
    case "Windows_NT" => "windows"
  }

  if (os == "linux" || os == "windows") {
    _updateDir = "/tmp/updateDir"
  }
  if (js.Dynamic.global.os.arch().asInstanceOf[String] == "x64") {
    os = os ++ "_64"
  }

  def versionCompare(str1: String, str2: String) = {


    val vals1 = str1.split("\\.")
    val vals2 = str2.split("\\.")
    var i = 0
    // set index to first non-equal ordinal or length of shortest version string
    while (i < vals1.length && i < vals2.length && vals1(i) == vals2(i)) {
      i += 1
    }
    // compare first non-equal ordinal number
    if (i < vals1.length && i < vals2.length) {
      val diff = Integer.valueOf(vals1(i)).compareTo(Integer.valueOf(vals2(i)))
      Integer.signum(diff)
    } else {
      Integer.signum(vals1.length - vals2.length)
    }
  }

  var newVersion = ""

  def isNewVersion(): Future[Boolean] = {
    var api = new ApiUpdateRestClient(httpClient)
    api.lastVersion(os) map {(latest) =>
      println("latest", latest, "current",autoUpdate.manifest("version").asInstanceOf[String] )
      newVersion = latest
      versionCompare(latest,autoUpdate.manifest("version").asInstanceOf[String]) > 0
    }
  }

  def download(): Future[String] = {
    js.Dynamic.global.request.download(
      autoUpdate.manifest("manifestUrl").asInstanceOf[String]
      ++ "download/"
      ++ "channel/stable" ++ "/" ++ os,
      _downloadDir,
      js.Dynamic.global.debounce({(e: js.Any) => js.Dynamic.global.console.log(e)},50).asInstanceOf[js.Function]
    ).asInstanceOf[scalajs.js.Promise[String]].toFuture
  }

  def restartToUpdate(): Future[Unit] = {
    new ChromeGlobalPreferences("update").edit().putBoolean("readyToUpdate", false).commit()
    autoUpdate.restartToSwap().toFuture.map((_) => ())
  }

  def restartIsNeeded(): Option[String] = {
    if (new ChromeGlobalPreferences("update").boolean("readyToUpdate").getOrElse(false)) {
      println("flag found")
      Some(autoUpdate.manifest("version").asInstanceOf[String])
    } else {
      None
    }
  }

  def updateProcess(): Future[Option[String]] = {
    println("checking new updates")
    isNewVersion().flatMap({ (test) =>
      if (test) {
        print("test version", test)
        println("downloading")
        download() flatMap {(updateFile) =>
        //Future.successful() flatMap {(updateFile) =>
          println("download returned", updateFile)
          autoUpdate.unpack(updateFile).toFuture flatMap { (updateDir) =>
          //Future.successful(_updateDir) flatMap { (updateDir) =>
            new ChromeGlobalPreferences("update").edit().putBoolean("readyToUpdate", true).commit()
            println("flag is ",new ChromeGlobalPreferences("update").boolean("readyToUpdate"))
            Future.successful(None)
          } recover({
            case e:Throwable => e.printStackTrace()
              None
          })
        }
      } else {
        Future.successful(None)
      }
    })
  }
}

