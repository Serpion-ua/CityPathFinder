package aggregation

import model.WeightedEdge

/**
 * Class for gather various statistical information for building heuristic for path finder
 * @param averageStreetDistance average distance between street nodes in particular graph
 */
final case class DataStatistic(averageStreetDistance: Double)

object DataStatistic {
  def apply(edges: Seq[WeightedEdge]): DataStatistic = {
    val oneBlockDistances =
      edges.collect{case WeightedEdge(from, to, distance) if from.manhattanDistance(to) == 1 => distance}

    val averageDistance = oneBlockDistances.sum(Numeric[Double]) / oneBlockDistances.size
    DataStatistic(averageDistance)
  }
}
