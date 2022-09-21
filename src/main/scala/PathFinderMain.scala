import com.typesafe.scalalogging.StrictLogging
import io.circe.syntax.EncoderOps
import json.JsonWriter.fullPathEncoder
import logic.FullPath

import scala.language.postfixOps

object PathFinderMain extends App with StrictLogging {
  val result: Either[Throwable, FullPath] = PathFinder.find(args)

  result match {
    case Left(error) =>
      logger.error(s"Failed to find path due: ${error.getMessage}")
    case Right(path) =>
      logger.info(s"Successfully find path: ${path.toString}");
      println(path.asJson)
  }
}
