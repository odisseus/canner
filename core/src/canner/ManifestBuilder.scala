package canner

import java.util.jar.{Attributes, Manifest}
import Attributes.Name._

case class ManifestBuilder(attributes: Map[AnyRef, AnyRef] = Map(MANIFEST_VERSION -> "1.0", "Created-By" -> "canner")) {

  def updated(key: Attributes.Name, value: AnyRef): ManifestBuilder = ManifestBuilder(attributes + (key -> value))

  def updated(key: String, value: String): ManifestBuilder = ManifestBuilder(attributes + (key -> value))

  def mainClass(className: String): ManifestBuilder = updated(MAIN_CLASS, className)

  def classPath(classNames: Set[String]): ManifestBuilder = updated(CLASS_PATH, classNames.mkString(" "))

  def build: Manifest = {
    val result = new Manifest
    val mainAttributes = result.getMainAttributes
    attributes.foreach{
      case (k: Attributes.Name, v) =>  mainAttributes.put(k, v)
      case (k, v) =>  mainAttributes.putValue(k.toString, v.toString)
    }
    result
  }

}


