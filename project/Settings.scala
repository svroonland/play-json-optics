import sbt._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object Settings {

  lazy val settings = Seq(
    organization := "nl.vroste",
    version := "0.0.1" + sys.props.getOrElse("buildNumber", default="0-SNAPSHOT"),
    scalaVersion := "2.12.5",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    assemblyJarName in assembly := "playjsonoptics-" + version.value + ".jar",
    test in assembly := {},
    target in assembly := file(baseDirectory.value + "/../bin/"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(
      includeScala = false,
      includeDependency=true),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case n if n.startsWith("reference.conf") => MergeStrategy.concat
      case _ => MergeStrategy.first
    },
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.7",
      "com.github.julien-truffaut" %%  "monocle-core"  % "1.5.1-cats",
      "org.typelevel" %% "alleycats-core" % "1.1.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    ),
    scalacOptions ++= Seq("-language:higherKinds"),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
  )

  lazy val testSettings = Seq(
    fork in Test := false,
    parallelExecution in Test := false
  )
}
