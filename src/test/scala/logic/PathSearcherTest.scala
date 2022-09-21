package logic

import aggregation.DataStatistic
import common.{UserFriendlyOutputSyntax, toDistanceConverter}
import model._
import util.{buildTestNode, buildTestWeightedEdge}



class PathSearcherTest extends org.specs2.mutable.Specification {

  "Path searcher" should {
    "work for simple cases" in {
      val edges: List[WeightedEdge] = List(
        buildTestWeightedEdge(0, 0, 0, 1, 34.0),
        buildTestWeightedEdge(0, 0, 1, 0, 12.0),
        buildTestWeightedEdge(0, 0, 1, 1, 45.0),
        buildTestWeightedEdge(0, 1, 1, 1, 7.0),
        buildTestWeightedEdge(1, 0, 1, 1, 13.0),
        buildTestWeightedEdge(1, 1, 2, 2, 4.0),
      )

      val pathHeuristic = new PathHeuristic(DataStatistic(edges))
      val weightedGraph = new WeightedGraph(edges)
      val pathSearcher = new PathSearcher(weightedGraph, pathHeuristic)

      val startNode = buildTestNode(0, 0)
      val endNode = buildTestNode(2, 2)

      val expectedNodePath = Seq(buildTestNode(0, 0), buildTestNode(1, 0), buildTestNode(1, 1), buildTestNode(2, 2))
      val expectedFullPathDistance = 12.0 + 13.0 + 4.0
      val expectedFullPath: FullPath = FullPath(startNode, endNode, expectedFullPathDistance.asDistanceType, expectedNodePath)

      val path = pathSearcher.findShortestPath(startNode, endNode, stopOnFirstFound = false)
      path must beRight(expectedFullPath)

      success
    }

    "find shortest path even for already reached end node" in {
      val edges: List[WeightedEdge] = List(
        //path to the end node (2, 0) will be found first because we will set big scaled manhattan distance
        buildTestWeightedEdge(0, 0, 1, 0, 10.0),
        buildTestWeightedEdge(1, 0, 2, 0, 12.0),

        //but actual short path have longer manhattan value
        buildTestWeightedEdge(0, 0, 0, 1, 1.0),
        buildTestWeightedEdge(0, 1, 0, 2, 1.1),
        buildTestWeightedEdge(0, 2, 0, 3, 1.2),
        buildTestWeightedEdge(0, 3, 1, 3, 1.3),
        buildTestWeightedEdge(1, 3, 2, 3, 1.4),
        buildTestWeightedEdge(2, 3, 2, 2, 1.5),
        buildTestWeightedEdge(2, 2, 2, 1, 1.6),
        buildTestWeightedEdge(2, 1, 2, 0, 1.7),

      )

      //for test consider manhattan distance scale at huge factor
      val pathHeuristic = new PathHeuristic(DataStatistic(50.0))
      val weightedGraph = new WeightedGraph(edges)
      val pathSearcher = new PathSearcher(weightedGraph, pathHeuristic)

      val startNode = buildTestNode(0, 0)
      val endNode = buildTestNode(2, 0)

      { //test case when we try to find first shortest path, and because of huge manhattan distance it will be path
        //according to manhattan distance
        val expectedNodePath = Seq(
          buildTestNode(0, 0), buildTestNode(1, 0), buildTestNode(2, 0))
        val expectedFullPathDistance = 10.0 + 12.0
        val expectedFullPath: FullPath = FullPath(startNode, endNode, expectedFullPathDistance.asDistanceType, expectedNodePath)

        val path = pathSearcher.findShortestPath(startNode, endNode, stopOnFirstFound = true)
        path must beRight(expectedFullPath)
      }

      { //test case when we try to find actual shortest path, thus we will not stop on first found path
        val expectedNodePath = Seq(
          buildTestNode(0, 0), buildTestNode(0, 1), buildTestNode(0, 2), buildTestNode(0, 3),
          buildTestNode(1, 3), buildTestNode(2, 3), buildTestNode(2, 2), buildTestNode(2, 1), buildTestNode(2, 0))
        val expectedFullPathDistance = 1.0 + 1.1 + 1.2 + 1.3 + 1.4 + 1.5 + 1.6 + 1.7
        val expectedFullPath: FullPath = FullPath(startNode, endNode, expectedFullPathDistance.asDistanceType, expectedNodePath)

        val path = pathSearcher.findShortestPath(startNode, endNode, stopOnFirstFound = false)
        path must beRight(expectedFullPath)
      }
      success
    }

    //Check that we stop searching if path became is too long.
    //It is correct behaviour in case if no edges with negative value (and this is expectation for our incoming data) are present.
    //However in that bundle we have edges with negative value,
    //thus actual shortest path will be unreachable, but we could test desired behaviour
    "we will stop searching in path if path is longer than already founded way" in {
      val edges: List[WeightedEdge] = List(
        //path to the end node (2, 0) will be found first because we will set big scaled manhattan distance
        buildTestWeightedEdge(0, 0, 1, 0, 10.0),
        buildTestWeightedEdge(1, 0, 2, 0, 12.0),

        //but actual short path have longer manhattan value
        buildTestWeightedEdge(0, 0, 0, 1, 1.0),
        buildTestWeightedEdge(0, 1, 0, 2, 1.1),
        buildTestWeightedEdge(0, 2, 0, 3, 1.2),
        buildTestWeightedEdge(0, 3, 1, 3, 1.3),
        buildTestWeightedEdge(1, 3, 2, 3, 1.4),
        buildTestWeightedEdge(2, 3, 2, 2, 22.0),
        buildTestWeightedEdge(2, 2, 2, 1, -20.0),
        buildTestWeightedEdge(2, 1, 2, 0, 1.7),

      )

      //for test consider manhattan distance scale at huge factor
      val pathHeuristic = new PathHeuristic(DataStatistic(50.0))
      val weightedGraph = new WeightedGraph(edges)
      val pathSearcher = new PathSearcher(weightedGraph, pathHeuristic)

      val startNode = buildTestNode(0, 0)
      val endNode = buildTestNode(2, 0)


      val expectedNodePath = Seq(
        buildTestNode(0, 0), buildTestNode(1, 0), buildTestNode(2, 0))
      val expectedFullPathDistance = 10.0 + 12.0
      val expectedFullPath: FullPath = FullPath(startNode, endNode, expectedFullPathDistance.asDistanceType, expectedNodePath)

      val path = pathSearcher.findShortestPath(startNode, endNode, stopOnFirstFound = false)
      path must beRight(expectedFullPath)
    }

    "will return error if path is not reachable" in {
      val edges: List[WeightedEdge] = List(
        buildTestWeightedEdge(0, 0, 0, 1, 34.0),
        buildTestWeightedEdge(0, 0, 1, 0, 12.0),
        buildTestWeightedEdge(0, 0, 1, 1, 45.0),
        buildTestWeightedEdge(0, 1, 1, 1, 7.0),
        buildTestWeightedEdge(1, 0, 1, 1, 13.0),
        buildTestWeightedEdge(1, 1, 2, 2, 4.0),
        buildTestWeightedEdge(5, 1, 2, 3, 4.0),

      )

      val pathHeuristic = new PathHeuristic(DataStatistic(edges))
      val weightedGraph = new WeightedGraph(edges)
      val pathSearcher = new PathSearcher(weightedGraph, pathHeuristic)

      val startNode = buildTestNode(0, 0)
      val endNode = buildTestNode(2, 3)

      val path = pathSearcher.findShortestPath(startNode, endNode, stopOnFirstFound = false)
      path.isLeft mustEqual true
      val exception = path.left.getOrElse(throw new IllegalStateException())
      exception.toString mustEqual new RuntimeException(s"All possible paths are processed, end node ${endNode.asUserFriendly} is not reached").toString

      success
    }

    "will return correct error messages if start or end nodes is not reachable in proper way" in {
      val edges: List[WeightedEdge] = List(
        buildTestWeightedEdge(0, 0, 0, 1, 34.0),
        buildTestWeightedEdge(0, 0, 1, 0, 12.0),
        buildTestWeightedEdge(0, 0, 1, 1, 45.0),
        buildTestWeightedEdge(0, 1, 1, 1, 7.0),
        buildTestWeightedEdge(1, 0, 1, 1, 13.0),
        buildTestWeightedEdge(1, 1, 2, 2, 4.0),
      )

      val pathHeuristic = new PathHeuristic(DataStatistic(edges))
      val weightedGraph = new WeightedGraph(edges)
      val pathSearcher = new PathSearcher(weightedGraph, pathHeuristic)

      val incorrectStartNode = buildTestNode(2, 2)
      val incorrectStartNodeResult = pathSearcher.findShortestPath(incorrectStartNode, buildTestNode(2, 2), stopOnFirstFound = false)
      incorrectStartNodeResult.isLeft mustEqual true
      val incorrectStartNodeError = incorrectStartNodeResult.left.getOrElse(throw new IllegalStateException())
      incorrectStartNodeError.toString mustEqual new IllegalArgumentException(s"There is no outgoing transitions from ${incorrectStartNode.asUserFriendly}").toString

      val incorrectEndNode = buildTestNode(0, 0)
      val incorrectEndNodeResult = pathSearcher.findShortestPath(buildTestNode(0, 1), incorrectEndNode, stopOnFirstFound = false)
      incorrectEndNodeResult.isLeft mustEqual true
      val incorrectEndNodeError = incorrectEndNodeResult.left.getOrElse(throw new IllegalStateException())
      incorrectEndNodeError.toString mustEqual new IllegalArgumentException(s"There is no incoming transitions to ${incorrectEndNode.asUserFriendly}").toString

      success
    }
  }
}
