name := "ledger-wallet-ethereum-chrome"

version := "1.0"

scalaVersion := "2.11.8"

enablePlugins(ScalaJSPlugin)

val build = taskKey[Unit]("Build the chrome packaged app")

persistLauncher := true
relativeSourceMaps := true

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.8.0"
libraryDependencies += "biz.enef" %%% "scalajs-angulate" % "0.2.4"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.sonatypeRepo("releases")

seq(lessSettings:_*)

build := {
  val appDir = target(_/"chrome-app").value
  appDir.mkdir()
  val resDir = (resourceDirectory in Compile).value

  // Copy all resources in chrome unpackaged directory
  IO.copyDirectory(resDir, appDir)
  println(s"AppDir is ${appDir.absolutePath}")
  println(s"ResDir is ${resDir.absolutePath}")

  // Build the application and copy to app directory
  val sourceFile = fastOptJS.in(Compile).value.data
  IO.copyFile(sourceFile, new File(appDir, sourceFile.name))
  val mapSourceFile = new File(sourceFile.absolutePath + ".map")
  if (mapSourceFile.exists())
    IO.copyFile(sourceFile, new File(appDir, mapSourceFile.name))
  val launcherFile = new File(sourceFile.getParent, name.value + "-launcher.js")
  IO.copyFile(launcherFile, new File(appDir, launcherFile.name))

  // Copy less files in bundle
  IO.copyDirectory(new File(sourceFile.getParentFile, "resource_managed/main/css"), appDir)
  ()
}

build <<= build.dependsOn(fastOptJS in Compile)
