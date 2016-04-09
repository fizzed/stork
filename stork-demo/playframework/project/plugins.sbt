resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.6")

// web plugins

addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

// just for testing need local maven repo
resolvers += Resolver.mavenLocal

// read app version from parent pom file
val pom = scala.xml.XML.load(scala.xml.Source.fromFile(new File("../../pom.xml")))

val appVersion = (pom \ "version").text

// Stork Play plugin (auto enabled as its an AutoPlugin)
addSbtPlugin("com.fizzed" % "fizzed-stork-sbt-play-plugin" % appVersion)
