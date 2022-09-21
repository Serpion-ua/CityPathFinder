package aggregation

import json.JsonModel.{CityMeasurement, CityMeasurementBundle, StreetMeasurement}
import model.{AvenueIndexTag, Node, StreetIndexTag}
import org.specs2.mutable.Specification
import util.buildTestWeightedEdge

class DataTransformingTest extends Specification {
  "Avenue naming conversions" should {
    "works correctly from name to index" in {
      DataTransforming.avenueNameToIndex("A") mustEqual shapeless.tag[AvenueIndexTag][Int](0)
      DataTransforming.avenueNameToIndex("v") mustEqual shapeless.tag[AvenueIndexTag][Int]('v' - 'a')
      DataTransforming.avenueNameToIndex("AA") must throwA[IllegalArgumentException]
      DataTransforming.avenueNameToIndex("Я") must throwA[IllegalArgumentException]
    }

    "works correctly from index to name" in {
      DataTransforming.avenueIndexToName(shapeless.tag[AvenueIndexTag][Int](0)) mustEqual "A"
      DataTransforming.avenueIndexToName(shapeless.tag[AvenueIndexTag][Int](7)) mustEqual "H"
    }
  }

  "Street naming conversions" should {
    "works correctly from name to index" in {
      DataTransforming.streetNameToIndex("0") mustEqual shapeless.tag[StreetIndexTag][Int](0)
      DataTransforming.streetNameToIndex("15") mustEqual shapeless.tag[StreetIndexTag][Int](15)
    }

    "works correctly from index to name" in {
      DataTransforming.streetIndexToName(shapeless.tag[StreetIndexTag][Int](0)) mustEqual "0"
      DataTransforming.streetIndexToName(shapeless.tag[StreetIndexTag][Int](23)) mustEqual "23"
    }
  }

  "Build Node" should {
    "be correct parsed if data is valid" in {
      val builtNode = DataTransforming.buildNode("f", "67")
      val expectedNode = Node(shapeless.tag[AvenueIndexTag][Int](5), shapeless.tag[StreetIndexTag][Int](67))
      builtNode mustEqual expectedNode
    }

    "throw exception if avenue name is not correct data is not correct" in {
      DataTransforming.buildNode("fa", "67") must throwA[IllegalArgumentException]
    }

    "throw exception if street name is not correct data is not correct" in {
      DataTransforming.buildNode("A", "B") must throwA[IllegalArgumentException]
    }
  }

  "Json data" should {
    "be corrected parsed if all data is correct for no measurement set is present" in {
      val jsonModel = CityMeasurementBundle(Seq())


      val parsedEither = DataTransforming.jsonModelToInternal(jsonModel)
      parsedEither.isRight mustEqual true
      val parsed = parsedEither.getOrElse(throw new IllegalStateException())

      parsed.size mustEqual 0

      success
    }

    "be corrected parsed if all data is correct for one measurement set" in {
      val streetMeasurements1 =
        Seq(StreetMeasurement("A", "1", "A", "2", 0.1), StreetMeasurement("A", "3", "Z", "8", 0.5))
      val cityMeasurement0 = CityMeasurement(555, streetMeasurements1)
      val jsonModel = CityMeasurementBundle(Seq(cityMeasurement0))


      val parsedEither = DataTransforming.jsonModelToInternal(jsonModel)
      parsedEither.isRight mustEqual true
      val parsed = parsedEither.getOrElse(throw new IllegalStateException())

      parsed.size mustEqual streetMeasurements1.size

      //("A", "1", "A", "2", 0.1)
      parsed.count(edge => edge == buildTestWeightedEdge(0, 1, 0, 2, 0.1)) == 1

      //("A", "3", "Z", "8", 0.5)
      parsed.count(edge => edge == buildTestWeightedEdge(0, 2, 25, 7, 0.5)) == 1

      success
    }

    "be corrected parsed if all data is correct for two measurement set" in {
      val streetMeasurements1 =
        Seq(StreetMeasurement("A", "1", "A", "2", 0.1), StreetMeasurement("A", "3", "Z", "8", 0.5))
      val streetMeasurements2 =
        Seq(StreetMeasurement("A", "1", "A", "2", 0.1), StreetMeasurement("C", "3", "Z", "8", 45.9))
      val cityMeasurement0 = CityMeasurement(555, streetMeasurements1)
      val cityMeasurement1 = CityMeasurement(777, streetMeasurements2)
      val jsonModel = CityMeasurementBundle(Seq(cityMeasurement0, cityMeasurement1))


      val parsedEither = DataTransforming.jsonModelToInternal(jsonModel)
      parsedEither.isRight mustEqual true
      val parsed = parsedEither.getOrElse(throw new IllegalStateException())

      parsed.size mustEqual streetMeasurements1.size + streetMeasurements2.size

      //("A", "1", "A", "2", 0.1)
      parsed.count(edge => edge == buildTestWeightedEdge(0, 1, 0, 2, 0.1)) == 2

      //("A", "3", "Z", "8", 0.5)
      parsed.count(edge => edge == buildTestWeightedEdge(0, 2, 25, 7, 0.5)) == 1

      //("C", "3", "Z", "8", 45.9)
      parsed.count(edge => edge == buildTestWeightedEdge(2, 2, 25, 7, 45.9)) == 1

      success
    }

    "be fail to parse if avenue name is missing" in {
      val streetMeasurements1 =
        Seq(StreetMeasurement("", "1", "A", "2", 0.1), StreetMeasurement("A", "3", "Z", "8", 0.5))
      val streetMeasurements2 =
        Seq(StreetMeasurement("A", "1", "A", "2", 0.1), StreetMeasurement("C", "3", "Z", "8", 45.9))
      val cityMeasurement0 = CityMeasurement(555, streetMeasurements1)
      val cityMeasurement1 = CityMeasurement(777, streetMeasurements2)
      val jsonModel = CityMeasurementBundle(Seq(cityMeasurement0, cityMeasurement1))


      val parsedEither = DataTransforming.jsonModelToInternal(jsonModel)
      parsedEither.isLeft mustEqual true
      val exception = parsedEither.left.getOrElse(throw new IllegalStateException())
      exception.toString mustEqual new IllegalArgumentException("Avenue name expected to be one letter").toString

      success
    }

    "be fail to parse if distance is less than 0" in {
      val streetMeasurements1 =
        Seq(StreetMeasurement("A", "1", "A", "2", -0.1), StreetMeasurement("A", "3", "Z", "8", 0.5))
      val streetMeasurements2 =
        Seq(StreetMeasurement("A", "1", "A", "2", 0.1), StreetMeasurement("C", "3", "Z", "8", 45.9))
      val cityMeasurement0 = CityMeasurement(555, streetMeasurements1)
      val cityMeasurement1 = CityMeasurement(777, streetMeasurements2)
      val jsonModel = CityMeasurementBundle(Seq(cityMeasurement0, cityMeasurement1))


      val parsedEither = DataTransforming.jsonModelToInternal(jsonModel)
      parsedEither.isLeft mustEqual true
      val exception = parsedEither.left.getOrElse(throw new IllegalStateException())
      exception.toString mustEqual new IllegalArgumentException("Distance can't be lower than 0").toString

      success
    }
  }
}
