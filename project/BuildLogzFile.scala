import java.io.{File, _}

class BuildLogzFile {

  def build(target: File, APIToken: String): File = {
    val file = new File(target, "LogzManager.scala")
    val out = new BufferedWriter(new FileWriter(file))
    val listener = "listener.logz.io"
    out << "package co.ledger.wallet.web.ripple.logz"
    out << "import scala.scalajs.js"
    out << "import co.ledger.wallet.web.ripple.core.utils.ChromeGlobalPreferences"
    out << "object LogzManager {"
    if (APIToken.length > 0) {
      out << s" val APIToken: Option[String] = Some(${APIToken.stringify})"
      out << " def init(): Unit = {"
      out << s"  js.Dynamic.global.logger = js.Dynamic.global.logzio.createLogger(js.Dynamic.literal(token=${APIToken.stringify}, host=${listener.stringify}))"
      out << " }"
      out << " def log(ex: String): Unit ={"
      out << "   js.Dynamic.global.logger.log(ex)"
      out << " }"
    } else {
      out << " val APIToken: Option[String] = None"
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

object BuildLogzFile {

}