import model.{Distance, Node, HeuristicPriority}

package object logic {
  final case class PathSegment(to: Node, from: Node, distanceToPreviousNode: Distance, totalDistance: Distance)
  final case class PrioritizedPathSegment(priority: HeuristicPriority, segment: PathSegment)
}
