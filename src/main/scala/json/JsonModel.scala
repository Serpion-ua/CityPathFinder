package json

/**
 * Onr to one representation of data parsed from JSON
 */
object JsonModel {
  final case class StreetNode(avenue: String, street: String)
  final case class StreetMeasurement(startAvenue: String, startStreet: String, endAvenue: String, endStreet: String, transitTime: Double)
  final case class CityMeasurement(measurementTime: Long, measurements: Seq[StreetMeasurement])
  final case class CityMeasurementBundle(trafficMeasurements: Seq[CityMeasurement])
}
