package canner

import java.io.FileOutputStream
import java.nio.file.Path
import java.util.jar.{JarOutputStream, Manifest}

import scala.util.Try

/** Packages files and directories into JAR archives. */
object Canner {

  /** Creates or updates a JAR archive.
    *
    * @param inputs files and directories to add into the archive
    * @param destination the path to the resulting archive
    * @param manifest the manifest to include into the archive
    * @return `Success(())` if the operation succeeded, or a `Failure` otherwise
    */
  def packJar(inputs: Set[Path], destination: Path, manifest: Manifest): Try[Unit] = Try {
    val out = new JarOutputStream(new FileOutputStream(destination.toFile), manifest)
    out.finish()
    out.close()
    inputs.foreach(f => Zipper.pack(f, destination))
  }

}
