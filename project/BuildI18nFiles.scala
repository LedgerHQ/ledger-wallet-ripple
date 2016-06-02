import java.io.{File, StringWriter}

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
    buildI18nManifest(i18nDir)
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
      val filter = {
        if (language.contains("-")) {
          language.replace('-', '_')
        } else {
          language + "_*"
        }
      }
      val entry = I18nLanguageEntry(
        language,
        root("language", "name").flatMap(_.value).getOrElse("").toString,
        filter
      )
      _manifest = I18nManifest(_manifest.languages :+ entry)
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

  private def buildI18nManifest(dest: File): Unit = {
    import upickle.default._
    IO.write(new File(dest, "manifest.json"), write(_manifest))
  }

  private var _manifest = I18nManifest(Array())
}

object BuildI18nFiles {

  case class I18nManifest(languages: Array[I18nLanguageEntry])

  case class I18nLanguageEntry(code: String, name: String, keys: String)

}