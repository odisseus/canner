package canner

import java.net.URI
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileSystems, FileVisitResult, Files, Path, SimpleFileVisitor, StandardCopyOption}
import java.util.stream.Collectors
import java.util.zip.{ZipEntry, ZipFile, ZipOutputStream}

import scala.collection.JavaConverters._
import scala.util.Try

import mercator._

object Zipper {

  private[canner] def packfs(zip: Zip, destination: Path): Try[Path] = {
    val fs = FileSystems.newFileSystem(
      URI.create("jar:" + destination.toUri.toString),
      Map("create" -> "true").asJava,
      null
    )
    val result = zip.entries.toList.sorted.traverse { case (name, source) =>
      Try {
        Files.copy(source, fs.getPath("/").resolve(name), StandardCopyOption.REPLACE_EXISTING)
      }
    }.map{ _ => destination }
    fs.close()
    result
  }

  def pack(root: Path, destination: Path): Try[Path] = {
    val paths = if(Files.isDirectory(root)){
      val pathWalker = Files.walk(root)
      val toPack = pathWalker.collect(Collectors.toList[Path]).asScala.toList//.tail
      pathWalker.close()
      toPack
    } else List(root)

    packfs(Zip(paths.map{ path =>
      root.getParent.relativize(path).toString -> path
    }.toMap), destination)
  }

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

case class Zip(entries: Map[String, Path] = Map.empty){
  def and(source: Path, name: String): Zip = Zip(entries + (name -> source))
  def merge(other: Zip): Zip = Zip(entries ++ other.entries)
  def writeTo(destination: Path): Try[Path] = Zipper.packfs(this, destination)
}

