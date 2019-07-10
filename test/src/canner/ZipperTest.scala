package canner

import java.nio.file.{Files, Path, Paths}

import org.scalacheck.Arbitrary
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks._

class ZipperTest extends FlatSpec with Matchers {

  behavior of "Zipper"

  private val resources = Paths.get("test", "resources")

  it should "unzip a simple archive" in {
    val target = Files.createTempDirectory("zipper-test-")
    Zipper.unpack(resources.resolve("foo.zip"), target).get
    diff(resources.resolve("foo_zip"), target) shouldBe 0
  }

  it should "zip and unzip a single file" in {
    val source = resources.resolve("foo_zip/foo/bar.txt")
    val target = Files.createTempDirectory("zipper-test-")
    val archive = target.resolve("bar.txt.zip")
    Zipper.pack(source, archive).get
    val unpacked = target.resolve("unpacked")
    Zipper.unpack(archive, unpacked).get
    diff(source, unpacked.resolve("bar.txt")) shouldBe 0
  }

  it should "zip and unzip a directory" in {
    val source = resources.resolve("foo_zip")
    val target = Files.createTempDirectory("zipper-test-")
    val archive = target.resolve("foo.zip")
    Zipper.pack(source, archive).get
    val unpacked = target.resolve("unpacked")
    Zipper.unpack(archive, unpacked).get
    diff(source, unpacked.resolve("foo_zip")) shouldBe 0
  }

  it should "pass the round trip" in {
    val base = Files.createTempDirectory("zipper-test-")
    val genBase = Files.createDirectories(base.resolve("gen"))
    val archives = Files.createDirectories(base.resolve("archives"))
    val unpacked = Files.createDirectories(base.resolve("unpacked"))
    implicit val anyPath: Arbitrary[Path] = ArbitraryFileTree(genBase)
    forAll { (dir: Path) =>
      val archive = archives.resolve(s"${dir.getFileName.toString}-packed.zip")
      val newDir = unpacked.resolve(dir.getFileName.toString)
      Files.createDirectories(newDir)
      Zipper.pack(dir, destination = archive).get
      Zipper.unpack(archive, unpacked).get
      diff(dir, newDir) shouldBe 0
    }
  }

  it should "merge two directories" in {
    val source1 = resources.resolve("foo_zip/foo")
    val source2 = resources.resolve("baz_zip/foo")
    val target = Files.createTempDirectory("zipper-test-")
    val archive = target.resolve("foo+baz.zip")
    Zipper.pack(source1, archive).get
    Zipper.pack(source2, archive).get
    val unpacked = target.resolve("unpacked")
    Zipper.unpack(archive, unpacked).get
    val expected = Files.createDirectories(resources.resolve("foo+baz"))
    diff(expected, unpacked) shouldBe 0
  }

  private def diff(a: Path, b: Path): Int = {
    import sys.process._
    val command = s"diff -r $a $b"
    println(command)
    command.!
  }

}
