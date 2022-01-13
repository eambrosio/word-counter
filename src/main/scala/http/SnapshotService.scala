package http

import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.typesafe.scalalogging.LazyLogging
import model.CounterStatus
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SnapshotService[F[_]] {
  def retrieveStatus(): F[Either[String, CounterStatus]]
  def persistStatus(status: CounterStatus): F[Either[String, Unit]]
}

case class SnapshotServicePostgresImpl()(implicit session: SlickSession, ec:ExecutionContext)
    extends SnapshotService[Future]
    with LazyLogging {

  import session.profile.api._

  override def retrieveStatus(): Future[Either[String, CounterStatus]] = {
    Thread.sleep(1000)
    val query  = sql"SELECT status FROM counter_status WHERE id=(SELECT max(id) FROM counter_status)".as[String]
    val result = query.asTry.map {
      case Success(value) if value.isEmpty  =>
        Right(CounterStatus.empty())

      case Success(value) if value.nonEmpty =>
        Right(value.head.parseJson.convertTo[CounterStatus])

      case Failure(exception)               =>
        logger.error(s"error while retrieving last snapshot: ${exception.getMessage}")
        Left("No persistence available")
    }

    session.db.run(result)
  }

  override def persistStatus(status: CounterStatus): Future[Either[String, Unit]] = {
    val query = sqlu"INSERT INTO counter_status(status) values(${status.toJson.compactPrint})"

    val result = query.asTry.map {
      case Success(_)         =>
        Right()

      case Failure(exception) =>
        Left(s"Error while persisting CounterStatus: ${exception.getMessage}")
    }

    session.db.run(result)
  }

}
