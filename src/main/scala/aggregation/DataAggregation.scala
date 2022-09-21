package aggregation

import common.toDistanceConverter
import model.{Distance, DistanceTag, WeightedEdge}
import shapeless.tag

/**
 * Object for aggregation data. We require it because we could have many measurements for the same transition.
 * Thus those measurements shall be somehow aggregated
 */
object DataAggregation {
  val minimumCountForSmartAggregation: Int = 10

  def aggregateWeightedEdges(edges: Seq[WeightedEdge]): Seq[WeightedEdge] = {
    edges
      .map(edge => ((edge.from, edge.to), edge.distance))
      .groupMap(_._1)(_._2)
      .view
      .mapValues(DataAggregation.aggregateDistances)
      .map{case ((from, to), distance) => WeightedEdge(from, to, distance)}
      .toList
  }

  private def aggregateDistances(measurements: Seq[Double]): Distance = {
    if (measurements.size < minimumCountForSmartAggregation) {
      val averageDistance = measurements.sum / measurements.size
      averageDistance.asDistanceType
    } else {
      val dropCount = measurements.size / 10 //integer division here!

      //drop upper and lower 10% of measurements to lower impact of unusual/error measurements
      val averageDistanceWithoutExtremes =
        measurements.sorted.drop(dropCount).dropRight(dropCount).sum / (measurements.size - 2 * dropCount)
      averageDistanceWithoutExtremes.asDistanceType
    }
  }
}
