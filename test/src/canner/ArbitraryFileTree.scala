package canner

import java.nio.file.{Files, Path}

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Gen._

object ArbitraryFileTree {

  //FIXME Replace with https://gist.github.com/charleso/51b6950ecba6c7fd30b49449582d8f48
  private def unusedPath(baseDirectory: Path): Gen[Path] = for{
    name <- listOfN(5, alphaLowerChar).map(_.mkString)
    path = baseDirectory.resolve(name) if (Files.notExists(path))
  } yield path

  def file(baseDirectory: Path): Gen[Path] = {
    for {
      path <- unusedPath(baseDirectory)
      content <- listOf(Arbitrary.arbByte.arbitrary) if content.size > 0
    } yield {
      //println(f"Generating file ${content.size}%4d at $path")
      Files.createFile(path)
      Files.write(path, content.toArray)
      path
    }
  }

  def directory(baseDirectory: Path, maxDepth: Int = 5): Gen[Path] = {
    for {
      path <- unusedPath(baseDirectory)
      //_ = println(s"Generating directory at $path")
      _ = Files.createDirectory(path)
      _ <- listOfN(5, frequency(10 -> file(path), maxDepth -> directory(path, maxDepth - 1)))
    } yield path
  }

  def apply(base: Path): Arbitrary[Path] = Arbitrary(directory(base))

}
