package canner

import java.net.URI
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileSystems, FileVisitResult, Files, Path, SimpleFileVisitor, StandardCopyOption}
import java.util.stream.Collectors
import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

import scala.collection.JavaConverters._
import scala.util.Try

import mercator._

/** Creates and extracts ZIP archives. */
object Zipper {

  private[canner] def packfs(zip: Zip, destination: Path): Try[Path] = {
    val fs = FileSystems.newFileSystem(
      URI.create("jar:" + destination.toUri.toString),
      Map("create" -> "true").asJava,
      null
    )
    val result = zip.entries.toList.sorted.traverse { case (name, source) =>
      Try {
        val destination = fs.getPath("/").resolve(name)
        if(!(Files.isDirectory(source) && Files.isDirectory(destination))){
          Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING)
        }
      }
    }.map{ _ => destination }
    fs.close()
    result
  }

  /** Packs the file or directory into a ZIP archive.
    *
    * If `root` is a directory, the entry paths of each file and directory are mapped relative to `root`,
    * e. g. when the `root` directory is `"/a/b"`,
    * the file `"/a/b/c"` will be packaged as a ZIP entry at `"/b/c"`.
    *
    * If the `destination` archive already exists, the contents from `root` will be added to it,
    * overwriting any existing entries which have the same paths.
    *
    * @param root the file or directory to archive
    * @param destination the path to the resulting archive
    * @return the path to the resulting archive, or a `Failure` if the operation fails
    */
  def pack(root: Path, destination: Path): Try[Path] = {
    val paths = if(Files.isDirectory(root)){
      val pathWalker = Files.walk(root)
      val toPack = pathWalker.collect(Collectors.toList[Path]).asScala.toList
      pathWalker.close()
      toPack
    } else List(root)

    packfs(Zip(paths.map{ path =>
      root.getParent.relativize(path).toString -> path
    }.toMap), destination)
  }

  /** Unpacks the contents of a ZIP archive to the given path.
    *
    * For example, if an archive that contains files at `/a` and `/b/c` is to be unpacked into directory `x`,
    * this will result in new files at paths `x/a` and `x/b/c`.
    *
    * @param source the archive to unpack
    * @param destination where to put the unpacked files and directories.
    * @return A `Zip` containing the list of unpacked entry names and the paths where they have been unpacked,
    *         or a `Failure` if the operation fails
    */
  def unpack(source: Path, destination: Path): Try[Zip] = {
    for{
      zipFile  <- Try(new ZipFile(source.toFile))
      zipEntries = zipFile.entries().asScala.toList
      entries <- zipEntries.traverse{ zipEntry =>
        val name = zipEntry.getName
        val in = zipFile.getInputStream(zipEntry)
        val result = Try{
          val target = destination.resolve(name)
          if(name.endsWith("/")){
            Files.createDirectories(target)
          } else{
            Files.createDirectories(target.getParent)
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
          }
          (name, target)
        }
        in.close()
        result
      }
    } yield Zip(entries.toMap)
  }

  private class ZippingFileVisitor(sourcePath: Path, out: ZipOutputStream) extends SimpleFileVisitor[Path] {

    override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
      val entry = new ZipEntry(sourcePath.relativize(file).toString)
      val content = Files.readAllBytes(file)
      out.putNextEntry(entry)
      out.write(content, 0, content.length)
      out.closeEntry()
      FileVisitResult.CONTINUE
    }

  }

}

/** A data structure which contains `Path`s in the file system, paired with the ZIP entry names.
  * It can be used to assemble an archive from sources in different locations.
  *
  * @param entries a map of source paths, indexed by the names of their corresponding ZIP entries.
  */
case class Zip(entries: Map[String, Path] = Map.empty){
  def and(source: Path, name: String): Zip = Zip(entries + (name -> source))
  def merge(other: Zip): Zip = Zip(entries ++ other.entries)

  /** Packages the source files and directories with their corresponding entry names into a ZIP archive.
    *
    * @param destination the path to the resulting archive. If the archive already exists, it will be updated.
    * @return the path to the resulting archive, or a `Failure` if the operation fails
    */
  def writeTo(destination: Path): Try[Path] = Zipper.packfs(this, destination)
}

