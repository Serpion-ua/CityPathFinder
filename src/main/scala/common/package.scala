import aggregation.DataTransforming.{avenueIndexToName, streetIndexToName}
import json.JsonModel.StreetNode
import model.{Distance, DistanceTag, Node}
import shapeless.tag

import scala.annotation.unused

package object common {
  trait UserFriendlyOutput[T] {
    def asUserFriendly(@unused toRepresent: T): String
  }

  implicit val nodeUserFriendly: UserFriendlyOutput[Node] = node =>
    s"(${avenueIndexToName(node.x)}:${streetIndexToName(node.y)})"


  implicit class UserFriendlyOutputSyntax[T : UserFriendlyOutput](element: T){
    def asUserFriendly: String = implicitly[UserFriendlyOutput[T]].asUserFriendly(element)
  }

  def nodeToStreetNode(node: Node): StreetNode = {
    StreetNode(avenueIndexToName(node.x), streetIndexToName(node.y))
  }

  implicit class toDistanceConverter(distance: Double) {
    def asDistanceType: Distance = tag[DistanceTag][Double](distance)
  }
}
