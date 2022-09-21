import shapeless.tag
import shapeless.tag.@@

package object model {
  trait NodeIdTag
  type NodeId = String @@ NodeIdTag

  trait AvenueIndexTag
  type AvenueIndex = Int @@ AvenueIndexTag

  trait StreetIndexTag
  type StreetIndex = Int @@ StreetIndexTag

  trait DistanceTag
  type Distance = Double @@ DistanceTag
  val zeroDistance: Distance = tag[DistanceTag][Double](0)

  trait ManhattanDistanceTag
  type ManhattanDistance = Int @@ ManhattanDistanceTag

  trait HeuristicPriorityTag
  type HeuristicPriority = Double @@ HeuristicPriorityTag
}
