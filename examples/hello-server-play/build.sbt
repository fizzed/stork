
// read app version from parent pom file
val pom = scala.xml.XML.load(scala.xml.Source.fromFile(new File("../../pom.xml")))

version := (pom \ "version").text

organization := (pom \ "groupId").text

name := "hello-server-play"

organizationName := "Fizzed, Inc."

organizationHomepage := Some(url("http://fizzed.com"))

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

resolvers += Resolver.mavenLocal

//run in Compile <<= (run in Compile).dependsOn(storkRun)

// bind stork to generate launchers during compile
//compile in Compile <<= (compile in Compile).dependsOn(storkLauncherGenerate)

//mainClass in (Compile, run) := Some("play.core.server.NettyServer")
//mainClass in (Compile, run) := Some("co.fizzed.stork.bootstrap.PlayBootstrap")
