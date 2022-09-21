package model
import shapeless.tag

final case class PreviousStep(previousNode: Node, distance: Float)

final case class Node (x: AvenueIndex, y: StreetIndex) {
  val id: NodeId = tag[NodeIdTag][String](s"$x:$y")
  def manhattanDistance(other: Node): ManhattanDistance = {
    val manhattanDistance: Int = Math.abs(other.x - x) + Math.abs(other.y - y)
    tag[ManhattanDistanceTag][Int](manhattanDistance)
  }
}
