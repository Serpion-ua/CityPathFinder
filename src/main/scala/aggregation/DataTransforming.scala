package aggregation

import common.toDistanceConverter
import json.JsonModel.CityMeasurementBundle
import model._
import shapeless.tag

import scala.util.Try

/**
 * Transform data from JSON representation to internal representation
 */
object DataTransforming {
  def buildNode(avenueName: String, streetName: String): Node = {
    Node(avenueNameToIndex(avenueName), streetNameToIndex(streetName))
  }

  private val startAvenue = 'A'
  private val startAvenueAsInt = startAvenue.toInt
  private val endAvenue = 'Z'
  def avenueNameToIndex(avenueName: String): AvenueIndex = {
    if (avenueName.length != 1) {
      throw new IllegalArgumentException("Avenue name expected to be one letter")
    }
    val avenueLetter = avenueName.head.toUpper
    if (avenueLetter < startAvenue || avenueLetter > endAvenue ) {
      throw new IllegalArgumentException(s"Avenue letter $avenueLetter is out of allowed bounds $startAvenue-$endAvenue")
    }
    tag[AvenueIndexTag][Int](avenueLetter.toInt - startAvenueAsInt)
  }

  def avenueIndexToName(avenueIndex: AvenueIndex): String = {
    (avenueIndex + startAvenueAsInt).toChar.toString
  }

  def streetNameToIndex(streetName: String): StreetIndex = {
    tag[StreetIndexTag][Int](streetName.toInt)
  }

  def streetIndexToName(streetIndex: StreetIndex): String = {
    streetIndex.toString
  }

  private def buildDistance(distance: Double): Distance = {
    if (distance < 0.0) throw new IllegalArgumentException("Distance can't be lower than 0")
    distance.asDistanceType
  }

  def jsonModelToInternal(measurementBundle: CityMeasurementBundle): Either[Throwable, Seq[WeightedEdge]] = Try {
    val weightedEdges: Seq[(Node, Node, Distance)] =
      for {
        trafficMeasurement <- measurementBundle.trafficMeasurements
        streetMeasurement <- trafficMeasurement.measurements
      } yield {
        val from: Node = buildNode(streetMeasurement.startAvenue, streetMeasurement.startStreet)
        val to: Node = buildNode(streetMeasurement.endAvenue, streetMeasurement.endStreet)
        val distance: Distance = buildDistance(streetMeasurement.transitTime)
        (from, to, distance)
      }

    weightedEdges.map(WeightedEdge.tupled)
  }.toEither
}
