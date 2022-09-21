package json

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import json.JsonModel.{CityMeasurement, CityMeasurementBundle, StreetMeasurement}

trait JsonReader {
  def parseJson(resourcePath: String): Either[Throwable, CityMeasurementBundle]
}


object FileJsonReader extends JsonReader {
  implicit val streetMeasurementDecoder: Decoder[StreetMeasurement] = deriveDecoder
  implicit val cityMeasurementDecoder: Decoder[CityMeasurement] = deriveDecoder
  implicit val cityMeasurementBundleDecoder: Decoder[CityMeasurementBundle] = deriveDecoder

  override def parseJson(resourcePath: String): Either[Throwable, CityMeasurementBundle] = {
    val source = scala.io.Source.fromFile(resourcePath)
    val lines = try source.mkString finally source.close()

    parse(lines).flatMap(_.as[CityMeasurementBundle])
  }
}