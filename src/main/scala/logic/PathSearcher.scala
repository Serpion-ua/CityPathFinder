package logic

import com.typesafe.scalalogging.StrictLogging
import common.{UserFriendlyOutputSyntax, toDistanceConverter}
import model._

import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Try

/**
 * Provide searching of the shortest path for given graph by using given path heuristic
 * @param graph graph with all possible transitions
 * @param pathHeuristic heuristic for selection of next processed node
 */
class PathSearcher(graph: WeightedGraph, pathHeuristic: PathHeuristic) extends StrictLogging {

  /**
   * Search shortest path
   * @param startNode starting node
   * @param endNode desired end node
   * @param stopOnFirstFound shall we stop as soon as any path had been found
   * @return full path or error
   */
  def findShortestPath(startNode: Node, endNode: Node, stopOnFirstFound: Boolean): Either[Throwable, FullPath] = {
    if (!graph.nodeIdHaveOutgoingEdges(startNode.id)) {
      Left(new IllegalArgumentException(s"There is no outgoing transitions from ${startNode.asUserFriendly}"))
    } else if(!graph.nodeHaveIncomingEdges(endNode.id)) {
      Left(new IllegalArgumentException(s"There is no incoming transitions to ${endNode.asUserFriendly}"))
    } else {
      searchPath(startNode, endNode, stopOnFirstFound)
    }
  }

  private def searchPath(startNode: Node, endNode: Node, stopOnFirstFound: Boolean): Either[Throwable, FullPath] = {
    val pathSegmentsToProcess =
      new mutable.PriorityQueue[PrioritizedPathSegment]()(Ordering.by(_.priority))

    val initialSegment = PathSegment(startNode, startNode, zeroDistance, zeroDistance)
    val initialPrioritizedSegment =
      PrioritizedPathSegment(pathHeuristic.calculatePathPriority(initialSegment, endNode), initialSegment)
    pathSegmentsToProcess.addOne(initialPrioritizedSegment)

    val pathSegmentsStorage: PathStorage = new PathStorage(startNode, endNode)
    val searchResult = searchPathIteration(pathSegmentsToProcess, endNode, pathSegmentsStorage, stopOnFirstFound)
    searchResult match {
      case Left(errorMessage) =>
        logger.info(s"${errorMessage.getMessage}")
        Left(errorMessage)
      case Right((fullPath, statistic)) =>
        logger.info(s"Successfully found path from ${startNode.asUserFriendly} to ${endNode.asUserFriendly} with total distance ${fullPath.totalDistance}.\nStatistic for search is next: $statistic")
        Right(fullPath)
    }
  }

  @tailrec
  private def searchPathIteration(nodes: mutable.PriorityQueue[PrioritizedPathSegment],
                       endNode: Node,
                       pathStorage: PathStorage,
                       stopOnFirstFind: Boolean): Either[Throwable, (FullPath, Statistic)] = {
    Try(nodes.dequeue().segment).toOption match {
      case Some(PathSegment(currentNode, _, _, totalDistance)) =>
        val edgesForCurrentNode: Seq[WeightedEdge] = graph.edgesByNodeId(currentNode.id)

        val nextFullPaths: Seq[PathSegment] = edgesForCurrentNode.map{ we =>
            val newTotalDistance: Distance = (totalDistance + we.distance).asDistanceType
            PathSegment(we.to, currentNode, we.distance, newTotalDistance)}

        val acceptedFullSegments: Seq[PathSegment] =
          nextFullPaths.filter(fullPaths => pathStorage.tryToAddPathSegment(fullPaths))
        if (stopOnFirstFind && pathStorage.endNodeIsReached) {
          pathStorage.tryToBuildFullPath()
        }
        else {
          val prioritizedPathSegments = acceptedFullSegments.map{segment =>
            val priority = pathHeuristic.calculatePathPriority(segment, endNode)
            PrioritizedPathSegment(priority, segment)
          }
          nodes.addAll(prioritizedPathSegments)
          searchPathIteration(nodes, endNode, pathStorage, stopOnFirstFind)
        }
      case None =>
        if (pathStorage.endNodeIsReached) {
          pathStorage.tryToBuildFullPath()
        }
        else {
          Left(new RuntimeException(s"All possible paths are processed, end node ${endNode.asUserFriendly} is not reached"))
        }
    }
  }
}

object PathSearcher {
  final case class SearchStatistic(storageStatistic: Statistic) {
    override def toString: String = {
        s"Count of accepted paths: ${storageStatistic.acceptedSegmentsCount}. " +
        s"Count of discarded path because we known shortest path: ${storageStatistic.discardedDueBetterPathSegmentsCount}. " +
        s"Count of discarded path because known best path to the node is shorter: ${storageStatistic.discardedDueLongerKnownEndNodePath}"
    }
  }
}
