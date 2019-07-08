package canner

import java.io.FileOutputStream
import java.nio.file.Path
import java.util.jar.{Attributes, JarOutputStream, Manifest => Manifest}

import scala.util.Try

object Canner {

  type ManifestAttributes = Map[Attributes.Name, AnyRef]

  def packJar(inputs: Set[Path], destination: Path, manifest: Manifest): Try[Unit] = Try {
    val out = new JarOutputStream(new FileOutputStream(destination.toFile), manifest)
    out.finish()
    out.close()
    inputs.foreach(f => Zipper.pack(f, destination))
  }

  def manifest(version: String, classpath: Set[String], mainClass: Option[String]): Manifest = {
    import Attributes.Name._
    val result = new Manifest
    val mainAttributes = result.getMainAttributes
    mainAttributes.put(MANIFEST_VERSION, "1.0")
    mainClass.foreach(mainAttributes.put(MAIN_CLASS, _))
    mainAttributes.put(CLASS_PATH, classpath.mkString(" "))
    result
  }

}
