package canner

import java.nio.file.{Files, Path}

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._

sealed trait FileTree {
  def createAt(root: Path): Unit
}

object FileTree {
  // Based on https://gist.github.com/charleso/51b6950ecba6c7fd30b49449582d8f48

  case class File(contents: List[Byte]) extends FileTree {

    override def createAt(location: Path): Unit = Files.write(location, contents.toArray)

    override def toString = {
      val presentation =
        if (contents.isEmpty) "<empty>"
        else if (contents.length <= 16) s""""0x${contents.map(_.toHexString).mkString}""""
        else s"${contents.length} bytes"
      s"File($presentation)"
    }

  }

  case class Dir(children: Map[String, FileTree]) extends FileTree {

    override def createAt(root: Path): Unit = {
      Files.createDirectories(root)
      children.foreach { case (name, child) =>
        child.createAt(root.resolve(name))
      }
    }

    private def maxNesting: Int = (children.values.map{
      case _: File => 0
      case x: Dir => 1 + x.maxNesting
    } ++ List(0)).max

    override def toString = {
      val presentation =
        if (children.isEmpty) "<empty>"
        else if (children.size <= 16) children.map{ case (name, child) => s"$name -> $child" }.mkString(", ")
        else s"${children.size} children, $maxNesting levels"
      s"Dir ($presentation)"
    }

  }

  private def filename: Gen[String] = nonEmptyListOf(alphaLowerChar).map(_.mkString)

  def file: Gen[FileTree] = listOf(Arbitrary.arbByte.arbitrary).map(FileTree.File(_))

  def directory: Gen[FileTree] = {
    val children = for {
      name <- filename
      ft <- Gen.size.flatMap(s => Gen.resize(s / 2, fileTree))
    } yield name -> ft
    mapOf(children).map(FileTree.Dir(_))
  }

  private def fileTree: Gen[FileTree] = Gen.size.flatMap { s =>
    frequency((s / 20) -> directory, 10 -> file)
  }

  def withFiles[A](files: FileTree)(f: Path => A): A = {
    val dir = Files.createTempDirectory("canner-test-filetree-")
    try {
      val root = dir.resolve(filename.sample.get)
      files.createAt(root)
      f(root)
    } finally {
      // TODO consider replacing with something that isn't marked as experimental
      new scala.reflect.io.Directory(dir.toFile).deleteRecursively()
    }
  }

  val arbitrary: Arbitrary[FileTree] = Arbitrary(fileTree)

}
