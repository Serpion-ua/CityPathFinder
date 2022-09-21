import aggregation.DataTransforming.buildNode
import model.Node
import org.backuity.clist.util.Read
import org.backuity.clist.{Cli, Command, arg, opt}

import scala.util.Try


class Config extends Command("") {
  implicit val nameRead: Read[Node] = Read.reads[Node]("AvenueName:StreetName") { str: String =>
    val splited = str.split(":")
    if (splited.size != 2) throw new IllegalArgumentException(s"Node shall be described by pattern AvenueName:StreetName, but got $str")
    buildNode(splited(0), splited(1))
  }

  var filePath:  String  = arg[String] (description = "Path to the JSON file with traffic measurements")
  var startNode: Node    = arg[Node]   (description = "Start node in form AvenueName:StreetName")
  var endNode:   Node    = arg[Node]   (description = "End node in form AvenueName:StreetName")
  var fastPath:  Boolean = opt[Boolean](description = "Return first found path", abbrev = "fast-path", default = false)
}

object Config {
  def parse(args: Array[String]): Either[Throwable, Config] = Try{
    Cli.parse(args)
      .throwExceptionOnError()
      .withCommand(new Config)(identity)
      .get
  }.toEither
}
