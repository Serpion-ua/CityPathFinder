package json

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import logic.FullPath
import model.Node
import io.circe.generic.auto._
import io.circe.shapes._

object JsonWriter {
  implicit val nodeEncoder: Encoder[Node] = deriveEncoder
  implicit val fullPathEncoder: Encoder[FullPath] = deriveEncoder
}
