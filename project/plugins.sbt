logLevel := Level.Warn
resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")
addSbtPlugin("net.lullabyte" % "sbt-chrome-plugin" % "0.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.1")
