package logic

import aggregation.DataStatistic
import common.toDistanceConverter
import org.specs2.mutable.Specification
import util.buildTestNode

class PathHeuristicTest extends Specification {
  "Path heuristic" should {
    "correctly calculate priority" in {
      val statistic = DataStatistic(10.0)
      val heuristic = new PathHeuristic(statistic)
      val totalDistance = 50.0.asDistanceType
      val pathSegmentEndNode = buildTestNode(10, 10)
      val pathSegment = PathSegment(pathSegmentEndNode, buildTestNode(0, 0), 10.0.asDistanceType, totalDistance)
      val endNode = buildTestNode(20, 20)

      val priority = heuristic.calculatePathPriority(pathSegment, endNode)
      val expectedPriority = -250.0

      priority mustEqual expectedPriority
    }
  }
}
