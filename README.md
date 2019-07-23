# Canner
A small library for working with JAR and ZIP archives

#### Supported functions:
* Creating and extracting ZIP archives
* Creating JAR files complete with manifests

#### Not supported yet:
* Working with encrypted archives
* Metadata for archive entries
* Scala 2.13

### Installing the library into your project

#### [SBT](https://www.scala-sbt.org/)

Add the following line to your `build.sbt` file:

```sbtshell
libraryDependencies += "io.github.odisseus" %% "canner" % "0.1.0-SNAPSHOT"
```

#### [Fury](https://fury.build/)

To add the latest master version as a source dependency, run these commands:

```bash
fury repo add --url gh:odisseus/canner
fury import add --import canner:default
fury dependency add --link canner/core
```

Alternatively, the library can be added as a binary dependency:

```bash
fury binary add --binary io.github.odisseus:canner_2.12:0.1.0-SNAPSHOT
```
