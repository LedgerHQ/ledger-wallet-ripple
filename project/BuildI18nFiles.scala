import java.io.{File, _}

import BuildI18nFiles.{I18nLanguageEntry, I18nManifest}
import sbt._

import scala.annotation.tailrec

class BuildI18nFiles {

  def build(sources: File, target: File): Unit = {
    val localesDir = new File(sources, "locales")
    val i18nDir = new File(target, "_locales")
    for (file <- localesDir.listFiles()) {
      buildSingleFile(file, new File(i18nDir, file.getName))
    }
  }

  private def buildSingleFile(dir: File, dest: File): Unit = {
    val language = dir.getName
    val messagesFile = new File(dir, "messages.yml")
    val translationsDir = dest.getParentFile
    if (messagesFile.exists()) {
      val root = YmlNode.parse(messagesFile)
      var writer = new StringWriter()
      root.writeJson(writer)
      IO.write(new File(translationsDir, s"$language.json"), writer.toString)
      writer = new StringWriter()
      buildChromeI18nFile(root).writeJson(writer)
      IO.write(new File(dest, "messages.json"), writer.toString)
    }
  }

  private def buildChromeI18nFile(root: YmlNode): YmlNode = {
    val result = new YmlNode()

    def flatten(node: YmlNode): Unit = {
      for (child <- node.children) {
        flatten(child)
      }
      node.value foreach {(v) =>
        val n = new YmlNode(result)
        n.name = node.path.replace('.', '_')
        val message = new YmlNode(n)
        val description = new YmlNode(n)
        message.name = "message"
        message.value = v
        description.name = "description"
        description.value = ""
      }
    }

    @tailrec
    def iterate(index: Int): Unit = {
      if (index >= root.children.length) {
        // End it
      } else if (root.children(index).name == "application") {
        flatten(root.children(index))
      } else {
        iterate(index + 1)
      }
    }
    iterate(0)
    result
  }

  def buildManifest(sources: File, file: File): Unit = {
    var manifest = I18nManifest(Array())
    for (dir <- sources.listFiles()) {
      val language = dir.getName
      val messagesFile = new File(dir, "messages.yml")
      if (messagesFile.exists()) {
        val root = YmlNode.parse(messagesFile)
        val filter = {
          if (language.contains("-")) {
            language.replace('-', '_')
          } else {
            language + "_*"
          }
        }
        val entry = I18nLanguageEntry(
          language.replace('-', '_'),
          root("language", "name").flatMap(_.value).getOrElse("").toString,
          filter
        )
        manifest = I18nManifest(manifest.languages :+ entry)
      }
    }
    writeManifest(file, manifest)
  }

  private def writeManifest(file: File, manifest: I18nManifest): Unit = {
    val out = new BufferedWriter(new FileWriter(file))
    out << "package co.ledger.wallet.web.ethereum.i18n"
    out << "object I18nLanguagesManifest {"
    for (language <- manifest.languages) {
      out << s" val ${language.code} = new I18nLanguageEntry(${language.code.quote}, ${language.name.quote}, ${language.keys.quote})"
    }
    out << " val languages = Array("
    for (language <- manifest.languages) {
      var sep = ""
      if (language.code != manifest.languages.last.code) {
        sep = ","
      }
      out << s"  ${language.code}$sep"
    }
    out << " )"
    out <<
      """
        | case class I18nLanguageEntry(code: String, name: String, keys: String)
      """.stripMargin
    out << "}"
    out.close()
  }

  private implicit class BetterWriter(w: Writer) {
    def <<(str: String) = {
      w.append(str)
      w.append('\n')
    }
  }

  private implicit class BetterString(string: String) {
    def quote = "\"" + string + "\""
  }
}

object BuildI18nFiles {

  case class I18nManifest(languages: Array[I18nLanguageEntry])

  case class I18nLanguageEntry(code: String, name: String, keys: String)

}