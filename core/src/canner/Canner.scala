package canner

import java.io.FileOutputStream
import java.nio.file.Path
import java.util.jar.{JarOutputStream, Manifest}

import scala.util.Try

object Canner {

  def packJar(inputs: Set[Path], destination: Path, manifest: Manifest): Try[Unit] = Try {
    val out = new JarOutputStream(new FileOutputStream(destination.toFile), manifest)
    out.finish()
    out.close()
    inputs.foreach(f => Zipper.pack(f, destination))
  }

}
