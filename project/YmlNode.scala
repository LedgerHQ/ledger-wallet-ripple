import java.io.{FileReader, Reader, StringWriter, Writer}

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.events.{MappingEndEvent, MappingStartEvent, ScalarEvent}
import sbt._
import upickle.Js
import upickle._

import scala.collection.mutable.ArrayBuffer

class YmlNode(val parent: Option[YmlNode] = None) {
  parent.foreach(_.addChild(this))

  def this(parent: YmlNode) = this(Some(parent))
  def isRoot = parent.isEmpty
  def children = _children.toArray
  var name = ""
  def value: Option[Any] = _value
  def value_=(v: Any) = _value = Some(v)

  private def addChild(child: YmlNode): Unit = {
    _children += child
  }

  private val _children = ArrayBuffer[YmlNode]()
  private var _value: Option[Any] = None

  override def toString: String = {
    val writer = new StringWriter()
    writer.append(if (isRoot) "root" else name)
    writer.append(": ")
    writer.append(_value.getOrElse("").toString)
    for (child <- _children) {
      writer.append("\n ")
      writer.append(child.toString.replace("\n", "\n "))
    }
    writer.toString
  }

  def toJson: Js.Value = {
    if (_children.nonEmpty) {
      var seq = scala.collection.Seq[(String, Js.Value)]()
      for (child <- _children) {
        seq = seq :+ (child.name, child.toJson)
      }
      Js.Obj(seq:_*)
    }
    else {
      Js.Str(value.getOrElse("").toString)
    }
  }

  def writeJson(writer: Writer) = writer.write(json.write(toJson))
}

object YmlNode {

  def parse(reader: Reader): YmlNode = {
    val yaml = new Yaml()
    var node = new YmlNode()
    val it = yaml.parse(reader).iterator()
    while (it.hasNext) {
      it.next() match {
        case event: MappingStartEvent =>
          val n = new YmlNode(node)
          node = n
        case event: ScalarEvent =>
          if (node.value.isDefined || node.children.nonEmpty) {
            node = new YmlNode(node.parent)
          }
          if (node.name.isEmpty)
            node.name = event.getValue
          else {
            node.value = event.getValue
          }
        case event: MappingEndEvent =>
          node = node.parent.get
        case other =>
          println(other)
      }
    }
    node
  }

  def parse(file: File): YmlNode = {
    val reader = new FileReader(file)
    val node = parse(reader)
    reader.close()
    node
  }

}