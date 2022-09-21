package aggregation

import org.specs2.mutable.Specification
import util._

class DataAggregationTest extends Specification {
  "DataAggregation" should {
    "correctly works if only one measurements" in {
      val edges = Seq(
        buildTestWeightedEdge(0, 0, 1, 1, 5.0),
        buildTestWeightedEdge(0, 0, 1, 0, 4.3),
        buildTestWeightedEdge(1, 0, 1, 1, 2.1),
        buildTestWeightedEdge(0, 1, 1, 1, 88.0),
      ).sorted

      val aggregated = DataAggregation.aggregateWeightedEdges(edges).sorted

      aggregated mustEqual edges
    }

    "correctly works for measurements count less than minimumCountForSmartAggregation" in {
      val multiplyFactor: Double = 1.1
      val range = (0 until DataAggregation.minimumCountForSmartAggregation)

      val edges =
        range.map(index => buildTestWeightedEdge(index * multiplyFactor))

      val aggregated = DataAggregation.aggregateWeightedEdges(edges)

      aggregated.size mustEqual 1
      val sum = range.map(multiplyFactor * _).sum
      val expectedValue = sum / range.size
      aggregated.head mustEqual buildTestWeightedEdge(expectedValue)
    }

    "correctly works for measurements count equal to minimumCountForSmartAggregation" in {
      val multiplyFactor: Double = 1.2
      val range = (0 until DataAggregation.minimumCountForSmartAggregation)

      val edges =
        range.map(index => buildTestWeightedEdge(index * multiplyFactor))
      val edgesWithAbsurdValues = edges ++ Seq(buildTestWeightedEdge(Int.MaxValue))
      val aggregated = DataAggregation.aggregateWeightedEdges(edgesWithAbsurdValues)

      aggregated.size mustEqual 1
      val sum = range.tail.map(multiplyFactor * _).sum
      val expectedValue = sum / (range.size - 1)
      aggregated.head mustEqual buildTestWeightedEdge(expectedValue)
    }

    "correctly works for measurements count more than minimumCountForSmartAggregation" in {
      val multiplyFactor: Double = 1.2
      val range = (0 until DataAggregation.minimumCountForSmartAggregation)

      val edges =
        range.map(index => buildTestWeightedEdge(index * multiplyFactor))
      val edgesWithAbsurdValues = edges ++ Seq(buildTestWeightedEdge(0), buildTestWeightedEdge(Int.MaxValue))
      val aggregated = DataAggregation.aggregateWeightedEdges(edgesWithAbsurdValues)

      aggregated.size mustEqual 1
      val sum = range.map(multiplyFactor * _).sum
      val expectedValue = sum / range.size
      aggregated.head mustEqual buildTestWeightedEdge(expectedValue)
    }

    "correctly works for mixed measurements" in {
      val edges = Seq(
        buildTestWeightedEdge(0, 0, 1, 1, 5.0),
        buildTestWeightedEdge(0, 0, 1, 1, 5.0),
        buildTestWeightedEdge(0, 0, 1, 1, 20.0),
        buildTestWeightedEdge(0, 0, 1, 0, 4.3),
        buildTestWeightedEdge(1, 0, 1, 1, 2.1),
        buildTestWeightedEdge(0, 1, 1, 1, 88.0),
        buildTestWeightedEdge(0, 1, 1, 1, 0.0),
      ).sorted

      val expected = Seq(
        buildTestWeightedEdge(0, 0, 1, 1, 10.0),
        buildTestWeightedEdge(0, 0, 1, 0, 4.3),
        buildTestWeightedEdge(1, 0, 1, 1, 2.1),
        buildTestWeightedEdge(0, 1, 1, 1, 44.0)
      ).sorted

      val aggregated = DataAggregation.aggregateWeightedEdges(edges).sorted

      aggregated mustEqual expected
    }
  }
}
