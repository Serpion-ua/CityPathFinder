import aggregation.{DataAggregation, DataStatistic, DataTransforming}
import com.typesafe.scalalogging.StrictLogging
import common.UserFriendlyOutputSyntax
import json.FileJsonReader
import logic.{FullPath, PathHeuristic, PathSearcher}
import model.WeightedGraph

object PathFinder extends StrictLogging {
  def find(args: Array[String]): Either[Throwable, FullPath] = {
    for {
      config <- Config.parse(args)
      _ = welcomeMessage(config)
      parsedData <- FileJsonReader.parseJson(config.filePath)
      transformedData <- DataTransforming.jsonModelToInternal(parsedData)
      aggregatedData = DataAggregation.aggregateWeightedEdges(transformedData)
      dataStatistic = DataStatistic(aggregatedData)
      graph = new WeightedGraph(aggregatedData)
      pathSearcher = new PathSearcher(graph, new PathHeuristic(dataStatistic))
      res <- pathSearcher.findShortestPath(config.startNode, config.endNode, config.fastPath)
    } yield res
  }

  private def welcomeMessage(config: Config): Unit = {
    logger.info(s"Welcome to the path finder! We will try to find way from ${config.startNode.asUserFriendly} to ${config.endNode.asUserFriendly}")
  }
}
