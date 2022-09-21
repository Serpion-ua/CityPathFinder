package json

import json.JsonModel.{CityMeasurement, CityMeasurementBundle, StreetMeasurement}
import org.specs2.mutable.Specification

class FileJsonReaderTest extends Specification {
  "Json parsing" should {
    "be successfully if all data is correct" in {
      val path = getClass.getResource("/correct-data.json").getPath
      val parsedData = FileJsonReader.parseJson(path)

      val expectedData =
        CityMeasurementBundle(
          List(
            CityMeasurement(86544,
              List(
                StreetMeasurement("A", "1", "B", "1", 28.000987663134676),
                StreetMeasurement("A", "2", "A", "1", 59.71131185379898),
                StreetMeasurement("A", "2", "B", "2", 50.605942255619624))),
            CityMeasurement(96544,
              List(
                StreetMeasurement("A", "1", "B", "1", 29.000987663134676),
                StreetMeasurement("A", "2", "A", "1", 69.71131185379897),
                StreetMeasurement("A", "2", "C", "2", 57.605942255619624)))))

      parsedData must beRight(expectedData)
      success
    }

    "be failed if some data is not correct" in {
      val path = getClass.getResource("/incorrect-data-missing-avenue.json").getPath
      val parsedData = FileJsonReader.parseJson(path)

      parsedData.isLeft mustEqual true
      success
    }
  }
}
