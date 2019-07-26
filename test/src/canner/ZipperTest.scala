package canner

import java.nio.file.{Path, Paths}

import org.scalacheck.Arbitrary
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks._

class ZipperTest extends FlatSpec with Matchers {
  import java.nio.file.Files._

  behavior of "Zipper"

  private val resources = Paths.get("test", "resources")

  it should "unzip a simple archive" in {
    val target = createTempDirectory("zipper-test-")
    Zipper.unpack(resources.resolve("foo.zip"), target).get
    diff(resources.resolve("foo_zip"), target) shouldBe 0
  }

  it should "zip and unzip a single file" in {
    val source = resources.resolve("foo_zip/foo/bar.txt")
    val target = createTempDirectory("zipper-test-")
    val archive = target.resolve("bar.txt.zip")
    Zipper.pack(source, archive).get
    val unpacked = target.resolve("unpacked")
    val record = Zipper.unpack(archive, unpacked).get
    val unpackedFile = unpacked.resolve("bar.txt")
    record shouldBe Zip(Map(source.getFileName.toString -> unpackedFile))
    diff(source, unpackedFile) shouldBe 0
  }

  it should "zip and unzip a directory" in {
    val source = resources.resolve("foo_zip")
    val target = createTempDirectory("zipper-test-")
    val archive = target.resolve("foo.zip")
    Zipper.pack(source, archive).get
    val unpacked = target.resolve("unpacked")
    Zipper.unpack(archive, unpacked).get
    diff(source, unpacked.resolve("foo_zip")) shouldBe 0
  }

  it should "pass the round trip" in {
    implicit val anyPath: Arbitrary[FileTree] = FileTree.arbitrary
    forAll { (fileTree: FileTree) =>
      FileTree.withFiles(fileTree){ source =>
        val base = createTempDirectory("zipper-test-")
        val unpacked = createTempDirectory("zipper-test-unpacked-")
        val archive = base.resolve(source.getFileName.toString + "-packed.zip")
        Zipper.pack(source, archive).get
        val record = Zipper.unpack(archive, unpacked).get
        val archive2 = base.resolve(source.getFileName.toString + "-repacked.zip")
        record.writeTo(archive2).get
        diff(source.getParent, unpacked) shouldBe 0
        diff(archive, archive2) shouldBe 0
      }
    }
  }

  it should "merge two directories" in {
    val source1 = resources.resolve("foo_zip/foo")
    val source2 = resources.resolve("baz_zip/foo")
    val target = createTempDirectory("zipper-test-")
    val archive = target.resolve("foo+baz.zip")
    Zipper.pack(source1, archive).get
    Zipper.pack(source2, archive).get
    val unpacked = target.resolve("unpacked")
    Zipper.unpack(archive, unpacked).get
    val expected = createDirectories(resources.resolve("foo+baz"))
    diff(expected, unpacked) shouldBe 0
  }

  private def diff(a: Path, b: Path): Int = {
    import sys.process._
    val command = s"diff -r $a $b"
    println(command)
    command.!
  }

}
