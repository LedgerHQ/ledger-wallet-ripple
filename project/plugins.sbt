logLevel := Level.Warn
resolvers += Resolver.sonatypeRepo("snapshots")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.8")
addSbtPlugin("net.lullabyte" % "sbt-chrome-plugin" % "0.2.0")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
  Resolver.ivyStylePatterns)

resolvers += "softprops-maven" at "http://dl.bintray.com/content/softprops/maven"

addSbtPlugin("me.lessis" % "less-sbt" % "0.2.2")