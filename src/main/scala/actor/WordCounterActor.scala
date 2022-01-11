package actor

import actor.WordCounterActor._
import akka.actor.{Actor, Props}
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import model.CounterStatus
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class WordCounterActor()(implicit session: SlickSession, ec: ExecutionContext, t: Timeout)
    extends Actor
    with LazyLogging {
  import session.profile.api._

  def running(counterStatus: CounterStatus): Receive = {
    case UpdateCount(data)      =>
      val (k, v)    = (data.head._1, data.map(_._2).sum)
      val newStatus = counterStatus.update(k, v)
      context become running(newStatus)

    case RetrieveStatus      =>
      sender() ! counterStatus

    case PersistData =>
      session.db.run(sqlu"INSERT INTO counter_status(status) values(${counterStatus.toJson.compactPrint})")
      self ! DataPersisted

    case DataPersisted       =>
      logger.info("data persisted")

  }

  override def receive: Receive = {
    case RetrieveData =>
      val query = sql"SELECT status FROM counter_status WHERE id=(SELECT max(id) FROM counter_status)".as[String]
      session.db.run(query).onComplete {
        case Success(value) if value.isEmpty  =>
          logger.info("no previous status. Starting the actor with empty status...")
          context become running(CounterStatus.empty())
        case Success(value) if value.nonEmpty =>
          logger.info("previous status exists. Starting the actor with previous status...")
          val status = value.head.parseJson.convertTo[CounterStatus]
          context become  running(status)
        case Failure(exception)               =>
          logger.error(s"error while retrieving last snapshot: ${exception.getMessage}")
          logger.error("Starting actor with empty status...")
          context become running(CounterStatus.empty())
      }
  }

}

object WordCounterActor {
  case class UpdateCount(data: Seq[(String, Long)])
  case object PersistData
  case object RetrieveStatus
  case object DataPersisted
  case object RetrieveData

  def props()(implicit session: SlickSession, ec: ExecutionContext, t: Timeout): Props = Props(new WordCounterActor)
}
