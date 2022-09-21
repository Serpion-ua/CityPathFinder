package logic

import aggregation.DataStatistic
import model.{Node, HeuristicPriority, HeuristicPriorityTag}
import shapeless.tag

/**
 * Provide calculation of static priority for given path segment based on previously built data statistic
 * @param dataStatistic statistic built on graph
 */
class PathHeuristic(dataStatistic: DataStatistic){
  def calculatePathPriority(pathSegment: PathSegment, endNode: Node): HeuristicPriority = {
    val priorityValue: Double = -(pathSegment.totalDistance + scaledManhattanDistanceToEndNode(endNode, pathSegment.to))
    tag[HeuristicPriorityTag][Double](priorityValue)
  }

  private def scaledManhattanDistanceToEndNode(endNode: Node, checkedNode: Node): Double = {
    endNode.manhattanDistance(checkedNode) * dataStatistic.averageStreetDistance
  }
}
