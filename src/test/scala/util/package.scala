import common.toDistanceConverter
import logic.PathSegment
import model.{AvenueIndex, AvenueIndexTag, Distance, DistanceTag, Node, StreetIndex, StreetIndexTag, WeightedEdge}

package object util {
  def buildTestNode(x: Int, y: Int): Node = {
    val avenueIndex: AvenueIndex = shapeless.tag[AvenueIndexTag][Int](x)
    val streetIndex: StreetIndex = shapeless.tag[StreetIndexTag][Int](y)
    Node(avenueIndex, streetIndex)
  }

  def buildTestWeightedEdge(x1: Int, y1: Int, x2: Int, y2: Int, distance: Double): WeightedEdge = {
    val from: Node = buildTestNode(x1, y1)
    val to: Node = buildTestNode(x2, y2)
    WeightedEdge(from, to, distance.asDistanceType)
  }

  def buildTestWeightedEdge(distance: Double): WeightedEdge = buildTestWeightedEdge(0, 0, 0, 0, distance)

  def buildTestPathSegment(x1: Int, y1: Int, x2: Int, y2: Int, distanceToPrevious: Double, totalDistance: Double): PathSegment = {
    PathSegment(buildTestNode(x1, y1), buildTestNode(x2, y2), distanceToPrevious.asDistanceType, totalDistance.asDistanceType)
  }

  implicit val nodeOrdering: Ordering[Node] = Ordering.by(_.id)

  implicit val weightedEdgeOrdering: Ordering[WeightedEdge] = Ordering.by(e => (e.from, e.to, e.distance))
}
