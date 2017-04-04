import java.io.{FileReader, StringWriter}

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.events.ScalarEvent

name := "ledger-wallet-ripple-chrome"

updateOptions := updateOptions.value.withCachedResolution(true)

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(ScalaJSPlugin)

val build = taskKey[Unit]("Build the chrome packaged app")

persistLauncher := true
relativeSourceMaps := true

val circeVersion = "0.7.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.1"
libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.3.0"
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.3"
libraryDependencies += "biz.enef" %%% "scalajs-angulate" % "0.2.4"
libraryDependencies += "net.lullabyte" %%% "scala-js-chrome" % "0.2.1"
libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0"
libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
libraryDependencies += "com.lihaoyi" %%% "upickle" % "0.4.0"
libraryDependencies += "io.github.widok" %%% "scala-js-momentjs" % "0.1.5"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

lazy val root = (project in file(".")).enablePlugins(SbtWeb)
includeFilter in (Assets, LessKeys.less) := "common.less"

sourceGenerators in Compile <+= sourceManaged in Compile map { dir =>
  val file = dir / "co" / "ledger" / "wallet" / "web" / "ripple" / "i18n" / "I18nLanguagesManifest.scala"
  file.getParentFile.mkdirs()
  new BuildI18nFiles().buildManifest(new File("src/main/resources/locales"), file)
  Seq(file)
}

build := {
  val appDir = target(_/"chrome-app").value
  appDir.mkdir()
  val resDir = (resourceDirectory in Compile).value

  // Copy all resources in chrome unpackaged directory
  IO.copyDirectory(resDir, appDir)

  // Build the application and copy to app directory
  val sourceFile = fastOptJS.in(Compile).value.data
  IO.copyFile(sourceFile, new File(appDir, sourceFile.name))
  val mapSourceFile = new File(sourceFile.absolutePath + ".map")
  if (mapSourceFile.exists())
    IO.copyFile(mapSourceFile, new File(appDir, mapSourceFile.name))
  val launcherFile = new File(sourceFile.getParent, name.value + "-launcher.js")
  IO.copyFile(launcherFile, new File(appDir, launcherFile.name))

  // Copy less files in bundle
  IO.copyDirectory(new File(sourceFile.getParentFile.getParentFile, "web/less/main/stylesheets"), appDir)

  // Compile i18n files
  new BuildI18nFiles().build(resDir, appDir)
  ()
}

build <<= build.dependsOn(fastOptJS in Compile)
