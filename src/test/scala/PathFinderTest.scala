import io.circe.syntax.EncoderOps
import org.specs2.mutable.Specification
import json.JsonWriter.fullPathEncoder

class PathFinderTest extends Specification {
  "Path finder" should {
    "correctly find and return correct short path" in {
      val path = getClass.getResource("/sample-data.json").getPath
      val startNode = "a:1"
      val endNode = "R:27"
      val res = PathFinder.find(Array(path, startNode, endNode))

      res.isRight mustEqual true
      val json = res.getOrElse(throw new IllegalStateException()).asJson.noSpaces
      val expectedJson = "{\"startNode\":{\"avenue\":\"A\",\"street\":\"1\"},\"endNode\":{\"avenue\":\"R\",\"street\":\"27\"},\"totalDistance\":423.7610239040167,\"path\":[{\"avenue\":\"A\",\"street\":\"1\"},{\"avenue\":\"T\",\"street\":\"1\"},{\"avenue\":\"T\",\"street\":\"30\"},{\"avenue\":\"S\",\"street\":\"30\"},{\"avenue\":\"R\",\"street\":\"30\"},{\"avenue\":\"Q\",\"street\":\"30\"},{\"avenue\":\"P\",\"street\":\"30\"},{\"avenue\":\"P\",\"street\":\"29\"},{\"avenue\":\"P\",\"street\":\"28\"},{\"avenue\":\"P\",\"street\":\"27\"},{\"avenue\":\"P\",\"street\":\"26\"},{\"avenue\":\"Q\",\"street\":\"26\"},{\"avenue\":\"R\",\"street\":\"26\"},{\"avenue\":\"R\",\"street\":\"27\"}]}"
      json.strip() mustEqual expectedJson
      success
    }
  }
}
