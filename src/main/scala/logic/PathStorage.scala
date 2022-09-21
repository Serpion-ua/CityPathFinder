package logic

import com.typesafe.scalalogging.StrictLogging
import common.{UserFriendlyOutputSyntax, nodeToStreetNode, toDistanceConverter}
import json.JsonModel.StreetNode
import model.{Distance, Node, NodeId}

import scala.annotation.tailrec
import scala.collection.mutable

/**
 * Smart storage for particular path searching for already processed path segments.
 * Before accepting segment storage checks is segment is acceptable.
 * Segment could be NOT acceptable if, for example, we know better path to destination node in that segment, or we already
 * know some path to end node and proposed segment distance is bigger than known path to destination node.
 * Also storage store some kind of statistic for performed search. It could be usefully for algorithm tuning.
 *
 * Added segments shall not form cyclic paths.
 * @param startNode start node for particular search
 * @param endNode desired end node for path
 */
class PathStorage(startNode: Node, endNode: Node) extends StrictLogging{
  val processedNodes: mutable.Map[NodeId, PathSegment] = mutable.Map.empty

  //For statistic purposes only
  private var discardedDueBetterPathSegmentsCount = 0
  private var discardedDueShortestEndNodePathCount = 0
  private var acceptedSegmentsCount = 0
  def totalProcessedSegments: Int = discardedDueBetterPathSegmentsCount + discardedDueShortestEndNodePathCount + acceptedSegmentsCount

  /**
   * We will not accept any segment if that segment total distance is more than already know path distance.
   * We could do it, because we assume that no distance with negative value
   */
  private var bestKnownShortPath: Option[Distance] = None


  /**
   * Try to add path segment to the storage
   * @param newPathSegment segment to add
   * @return
   *  true if segment is accepted, i.e. it could be probably used for end node path building,
   *  false if segment is not accepted, i.e. will never be used for end node  path building
   */
  def tryToAddPathSegment(newPathSegment: PathSegment): Boolean = {
    val oldPathOpt: Option[PathSegment] = processedNodes.get(newPathSegment.to.id)

    if (segmentIsAcceptable(oldPathOpt, newPathSegment)) {
      acceptSegment(newPathSegment)
      true
    }
    else {
      logger.trace(s"Discard path segment $newPathSegment")
      false
    }
  }

  private def acceptSegment(segment: PathSegment): Unit = {
    val toNodeId: NodeId = segment.to.id
    processedNodes(toNodeId) = segment

    if (toNodeId == endNode.id) {
      tryToUpdateBestKnownPathDistance(segment.totalDistance)
    }

    logger.trace(s"Accept path segment $segment. It have path ${segment.totalDistance}")
    acceptedSegmentsCount = acceptedSegmentsCount + 1
  }

  private def tryToUpdateBestKnownPathDistance(newTotalDistance: Distance): Unit = {
    bestKnownShortPath = Option(bestKnownShortPath.fold(newTotalDistance)(Math.min(_, newTotalDistance).asDistanceType))
    logger.debug(s"Update know best path to value $newTotalDistance, total processed segments count is $totalProcessedSegments")
  }

  private def segmentIsAcceptable(oldSegmentOpt: Option[PathSegment], newSegment: PathSegment): Boolean = {
    val newPathTotalDistance = newSegment.totalDistance

    if (bestKnownShortPath.exists(_ < newPathTotalDistance)) {
      discardedDueShortestEndNodePathCount = discardedDueShortestEndNodePathCount + 1
      false
    }
    else {
      oldSegmentOpt match {
        case Some(oldSegment) =>
          val oldPathDistance = oldSegment.totalDistance
          if (newPathTotalDistance > oldPathDistance) {
            discardedDueBetterPathSegmentsCount = discardedDueBetterPathSegmentsCount + 1
            false
          } else {
            true
          }
        case None =>
          true
      }
    }
  }

  private def getCurrentStatistic: Statistic =
    Statistic(acceptedSegmentsCount, discardedDueBetterPathSegmentsCount, discardedDueShortestEndNodePathCount)

  /**
   * TODO fix infinite loop in case if we have cycles in path;
   * TODO start node is checked by totalDistance == distanceToPreviousNode, change it;
   * Try to build full path from start node to the end node
   * @return Full path and statistic if path to end node is reachable, error otherwise
   */
  def tryToBuildFullPath(): Either[Throwable, (FullPath, Statistic)] = {
    @tailrec
    def buildReverseIterate(acc: List[PathSegment], node: Node): Seq[PathSegment] = {
      if (acc.head.totalDistance == acc.head.distanceToPreviousNode) {
        acc //we have reached first segment
      }
      else {
        val previousSegment = processedNodes(node.id)
        buildReverseIterate(previousSegment :: acc, previousSegment.from)
      }
    }

    val nodeId: NodeId = endNode.id
    val fullPathOpt =
      processedNodes.get(nodeId).map{startSegment =>
        val pathSegments: Seq[PathSegment] = buildReverseIterate(List(startSegment), startSegment.from)
        val nodesPath = pathSegments.map(_.from) :+ startSegment.to
        FullPath(startNode, endNode, startSegment.totalDistance, nodesPath)}

    fullPathOpt match {
      case Some(fullPath) => Right(fullPath, getCurrentStatistic)
      case None => Left(new RuntimeException(s"No path is found from ${startNode.asUserFriendly} to node ${endNode.asUserFriendly}"))
    }
  }

  def endNodeIsReached: Boolean = processedNodes.contains(endNode.id)
}


final case class Statistic(acceptedSegmentsCount: Int, discardedDueBetterPathSegmentsCount: Int, discardedDueLongerKnownEndNodePath: Int) {
  override def toString: String = {
    s"Count of accepted paths is $acceptedSegmentsCount. " +
      s"Count of discarded path because we known shortest path is $discardedDueBetterPathSegmentsCount. " +
      s"Count of discarded path because known shorter path to the end node is $discardedDueLongerKnownEndNodePath"
  }
}


final case class FullPath(startNode: StreetNode, endNode: StreetNode, totalDistance: Distance, path: Seq[StreetNode]) {
  override def toString: String = {
    val pathString: String = path.map(node => s"($node)").mkString("->")
    s"Total distance is: $totalDistance. Path is $pathString"
  }
}

object FullPath {
  def apply(startNode: Node, endNode: Node, fullDistance: Distance, path: Seq[Node]): FullPath = {
    FullPath(nodeToStreetNode(startNode), nodeToStreetNode(endNode), fullDistance, path.map(nodeToStreetNode))
  }
}