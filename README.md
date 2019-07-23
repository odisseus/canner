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

### Using the library

#### Create a ZIP archive

```scala
import java.nio.file.{Path, Paths}
import scala.util.Try
import canner._

// These files or directories shall be packaged
val sourceFile: Path = Paths.get("path/to/some/file.txt")
val sourceDir: Path = Paths.get("path/to/a/directory")

// If the archive already exists, new entries will be added to it
val destination: Path = Paths.get("archive.zip")

// If the destination archive is successfully updated, `Zipper.pack` will return `Success(destination)`
val fileAdded: Try[Path] = Zipper.pack(sourceFile, destination)
val dirAdded: Try[Path] = Zipper.pack(sourceDir, destination)

// `archive.zip` now contains entries named `/file.txt` and `/directory/`
```

##### Create a ZIP archive using fluent interface

```scala
import java.nio.file.{Path, Paths}
import scala.util.Try
import canner._

val result: Try[Path] = Zip()
  .and(Paths.get("path/to/source.txt"), "source1.txt")
  .and(Paths.get("path/to/other/source.txt"), "sources/source2.txt")
  .and(Paths.get("path/to/yet/another/source"), "sources/dir")
  .writeTo(Paths.get("archive.zip"))
  
// `archive.zip` now contains entries named `/source1.txt`, `sources/source2.txt` and `/sources/dir/`
```


#### Create a runnable JAR file

```scala
import java.nio.file.Paths
import scala.util.Try
import canner._

val compiledClassesDirectories = Set(
  Paths.get("project-a/target/scala-2.12/classes"),
  Paths.get("project-b/target/scala-2.12/classes")
)
val jar = Paths.get("app.jar")

val manifest = ManifestBuilder()
  .mainClass("projectb.MainApp")
  .classPath(Set("dependency.jar", "otherDependency.jar", "path/to/anotherDependency.jar"))
  .updated("Created-By", "My custom packer")
  .build
  
val result: Try[Unit] = Canner.packJar(compiledClassesDirectories, jar, manifest)

// `app.jar` now contains classes from `project-a` and `project-b`, with `projectb.MainApp` defined as a main class
```

#### Unpack an archive
```scala
import java.nio.file.{Path, Paths}
import scala.util.Try
import canner._

val archive: Path = Paths.get("archive.zip")

val destination: Path = Paths.get("directory/to/extract/into")

// The result is a list of paths that have been created along with the names of corresponding entries in the archive.
// Calling result.writeTo(Path) will create an archive with the same contents as the initial one
val result: Try[Zip] = Zipper.unpack(archive, destination)

// The destination directory now contains all files and directories present in the archive
```

See the [tests](https://github.com/odisseus/canner/tree/master/test/src/canner) for more examples.
