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
      "org.scalatest" %% "scalatest" % "3.0.0" % "test")
  )
unmanagedBase := baseDirectory.value / "lib"


//classpathTypes in Runtime += baseDirectory.value + "/src/main/resources/"
//classpathTypes in Compile += baseDirectory.value + "/src/test/resources/"
//classpathTypes in Test += baseDirectory.value + "/src/test/resources/"

exportJars := true