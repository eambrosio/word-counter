package http

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, onComplete}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait EndpointDirectives extends LazyLogging {

  def responseFromFuture[T](future: Future[Either[String, T]])(implicit marshaller: ToResponseMarshaller[T]): Route =
    onComplete(future) {
      case Success(Right(value)) =>
        complete(value)
      case Success(Left(error))  =>
        complete(StatusCodes.BadRequest, error)
      case Failure(exception)    =>
        logger.error(exception.getLocalizedMessage)
        complete(StatusCodes.InternalServerError, exception.getLocalizedMessage)
    }

}
