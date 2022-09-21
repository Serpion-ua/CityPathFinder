package aggregation

import org.specs2.mutable.Specification
import util.buildTestWeightedEdge

class DataStatisticTest extends Specification {
  "DataStatistic" should {
    "correctly calculate average distance if all manhattan distances is 1" in {
      val distances = Seq(5.0, 5.0, 20.1, 4.3, 2.1, 88.0, 0.0)
      val nodeIndexes = Seq(
        (2, 1, 1, 1),
        (1, 2, 1, 1),
        (0, 1, 1, 1),
        (0, 0, 1, 0),
        (1, 0, 1, 1),
        (0, 1, 1, 1),
        (1, 0, 0, 0))

      val size = Math.min(distances.size, nodeIndexes.size)

      val edges = (0 until size)
        .map(i => (nodeIndexes(i), distances(i)))
        .map { case ((x1, y1, x2, y2), distance) => buildTestWeightedEdge(x1, y1, x2, y2, distance) }

      val statistic = DataStatistic(edges)
      statistic.averageStreetDistance mustEqual (distances.sum / distances.size)
    }

    "correctly calculate average distance if not all manhattan distances is 1" in {
      val distances = Seq(5.0, 5.0, 20.1, 4.3, 2.1, 88.0, 0.0)
      val nodeIndexes = Seq(
        (2, 1, 1, 1),
        (1, 2, 1, 1),
        (0, 1, 1, 1),
        (0, 0, 1, 0),
        (1, 0, 1, 1),
        (0, 1, 1, 1),
        (1, 0, 0, 0))

      val size = Math.min(distances.size, nodeIndexes.size)

      val edges = (0 until size)
        .map(i => (nodeIndexes(i), distances(i)))
        .map { case ((x1, y1, x2, y2), distance) => buildTestWeightedEdge(x1, y1, x2, y2, distance) }


      val edgesWithManhattanDistanceMoreThanOne = Seq(
        buildTestWeightedEdge(0, 0, 1, 1, 500.0),
        buildTestWeightedEdge(2, 0, 1, 1, 500.0),
        buildTestWeightedEdge(2, 2, 2, 2, 500.0))

      val statistic = DataStatistic(edges ++ edgesWithManhattanDistanceMoreThanOne)
      statistic.averageStreetDistance mustEqual (distances.sum / distances.size)
    }
  }
}
