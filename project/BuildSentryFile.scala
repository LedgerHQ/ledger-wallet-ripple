import java.io.{File, _}

class BuildSentryFile {

  def build(target: File, sentryURL: String): File = {
    val file = new File(target, "SentryManager.scala")
    val out = new BufferedWriter(new FileWriter(file))
    out << "package co.ledger.wallet.web.ripple.sentry"
    out << "import scala.scalajs.js"
    out << "import co.ledger.wallet.web.ripple.core.utils.ChromeGlobalPreferences"
    out << "object SentryManager {"
    if(sentryURL.length > 0) {
      out << s" val secret: Option[String] = Some(${sentryURL.stringify})"
      out << " def init(): Unit = {"
      out << s"  js.Dynamic.global.Raven.config(${sentryURL.stringify}, js.Dynamic.literal(allowSecretKey=true)).install()"
      out << " }"
      out << " def log(ex: String): Unit ={"
      out << "  if(new ChromeGlobalPreferences(\"Settings\").boolean(\"using_default\").getOrElse(true)) {"
      out << "   js.Dynamic.global.Raven.captureException(ex)"
      out << "  }"
      out << " }"
    } else {
      out << " val secret: Option[String] = None"
      out << " def init(): Unit = {}"
      out << " def log(ex: String): Unit = {}"
    }
    out << "}"
    out.close()
    file
  }

  private implicit class BetterWriter(w: Writer) {
    def <<(str: String) = {
      w.append(str)
      w.append('\n')
    }
  }

  private implicit class BetterString(s: String) {
    def stringify = "\"" + s + "\""
  }
}

object BuildSentryFile {

}