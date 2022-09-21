package model

import org.specs2.mutable.Specification
import util.buildTestNode

class NodeTest extends Specification {
  "Node" should {
    "correctly calculate manhattan distance" in {
      val fromNode = buildTestNode(10, 10)

      fromNode.manhattanDistance(buildTestNode(10, 11)) mustEqual 1
      fromNode.manhattanDistance(buildTestNode(11, 11)) mustEqual 2
      fromNode.manhattanDistance(buildTestNode(0, 0)) mustEqual 20
    }
  }
}
