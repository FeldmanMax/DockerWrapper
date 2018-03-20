import sbt.Keys.libraryDependencies

autoScalaLibrary := true
managedScalaInstance := false

lazy val commonSettings = Seq(
  organization := "max.feldman",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4"
)

lazy val root = (project in file("."))
  .settings(
    commonSettings,
    name := "DockerWrapper",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.1" % "test")
  )
unmanagedBase := baseDirectory.value / "lib"

exportJars := true