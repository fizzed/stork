resolvers += Resolver.mavenLocal

net.virtualvoid.sbt.graph.Plugin.graphSettings

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

// does not work with com.github.eirslett#sbt-slf4j_2.11;0.1:
//crossScalaVersions := Seq("2.10.4", "2.11.5")

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
    <url>https://github.com/fizzed/java-stork</url>
    <licenses>
        <license>
            <name>Apache 2</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>
    <scm>
        <url>https://github.com/fizzed/java-stork.git</url>
        <connection>scm:git:git@github.com:fizzed/java-stork.git</connection>
    </scm>
    <developers>
        <developer>
            <email>oss@fizzed.co</email>
        </developer>
    </developers>
)