package logic

import common.toDistanceConverter
import org.specs2.mutable.Specification
import util.{buildTestNode, buildTestPathSegment}

class PathStorageTest extends Specification {
  "Path storage" should {
    "Shall not accept segment with bigger distance, accept otherwise" in {
      val storage = new PathStorage(buildTestNode(0, 0), buildTestNode(10, 10))
      val pathSegment = buildTestPathSegment(0, 0, 1, 1, 5, 5)
      storage.tryToAddPathSegment(pathSegment) mustEqual true

      storage.tryToAddPathSegment(pathSegment.copy(totalDistance = (pathSegment.totalDistance + 1.0).asDistanceType)) mustEqual false

      val pathSegmentFromOtherNode = buildTestPathSegment(0, 0, 0, 1, 5, 5.1)
      storage.tryToAddPathSegment(pathSegmentFromOtherNode) mustEqual false
    }

    "Shall not accept segment if total distance is more than best known distance to the end node, accept otherwise" in {
      val storage = new PathStorage(buildTestNode(0, 0), buildTestNode(2, 0))
      val firstTotalDistance = 11.0
      val firstPathSegment = buildTestPathSegment(1, 0, 0, 0, 5, 5)
      storage.tryToAddPathSegment(firstPathSegment) mustEqual true

      val secondPathSegment = buildTestPathSegment(2, 0, 1, 0, 6, firstTotalDistance)
      storage.tryToAddPathSegment(secondPathSegment) mustEqual true

      val knownPath = storage.tryToBuildFullPath()
      knownPath.isRight mustEqual true
      knownPath.getOrElse(throw new IllegalStateException())._1.totalDistance mustEqual firstTotalDistance

      val segmentToWithExceedTotalDistance =
        buildTestPathSegment(0, 0, 0, 1, 6, firstTotalDistance + 0.1)
      //try to add as the path to the new node
      storage.tryToAddPathSegment(segmentToWithExceedTotalDistance) mustEqual false

      val thirdPathSegment =
        buildTestPathSegment(0, 0, 0, 1, 6, firstTotalDistance - 0.1)
      storage.tryToAddPathSegment(thirdPathSegment) mustEqual true

      //try to add as the path to the already known node
      storage.tryToAddPathSegment(segmentToWithExceedTotalDistance) mustEqual false
    }

    "build correct full path" in {
      val storage = new PathStorage(buildTestNode(0, 0), buildTestNode(10, 10))
      val segmentsRange = (0 until 10)
      segmentsRange
        .map(i => buildTestPathSegment(i + 1, i + 1, i, i, 1, i + 1))
        .foreach{segment =>
          storage.tryToAddPathSegment(segment) mustEqual true
        }

      val expectedNodes = (segmentsRange.start to segmentsRange.end).map(i => buildTestNode(i, i))
      val expectedFullPath = FullPath(buildTestNode(0, 0), buildTestNode(10, 10), (segmentsRange.last + 1).asDistanceType, expectedNodes)

      val res = storage.tryToBuildFullPath()
      res.isRight mustEqual true
      val fullPath = res.getOrElse(throw new IllegalStateException())._1
      fullPath mustEqual expectedFullPath
    }
  }
}
