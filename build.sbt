name := "canner"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.8"

scalaSource in Compile := baseDirectory.value / "core" / "src"

scalaSource in Test := baseDirectory.value / "test" / "src"

resourceDirectory in Test := baseDirectory.value / "test"/ "resources"

libraryDependencies += "com.propensive" %% "mercator" % "0.2.1"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

// POM settings for Sonatype
organization := "io.github.odisseus"
homepage := Some(url("https://github.com/odisseus/canner"))
scmInfo := Some(ScmInfo(url("https://github.com/odisseus/canner"), "git@github.com:odisseus/canner.git"))
developers := List(Developer("odisseus",
  "Myroslav Golub",
  "mgolub@virtuslab.com",
  url("https://github.com/odisseus")))
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
publishMavenStyle := true

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
