package canner

import java.nio.file.{Files, Paths}

import org.scalatest._

class CannerTest extends FlatSpec with Matchers {

  behavior of "Canner"

  private val resources = Paths.get("test", "resources")

  it should "package a directory with classes and add the manifest" in {
    import sys.process._
    val source = resources.resolve("hello")
    val target = Files.createTempDirectory("canner-test-")
    val jar = target.resolve("hello.jar")
    val manifest = ManifestBuilder().mainClass("hello.HelloWorld").build
    Canner.packJar(Set(source), jar, manifest).get
    val command = s"java -jar $jar"
    println(command)
    command.!! shouldBe "Hello World!\n"
  }

}
