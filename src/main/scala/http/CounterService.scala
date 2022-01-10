package http

import actor.WordCounterActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import model.CounterStatus

import scala.concurrent.Future

trait CounterService[F[_]] {
  def getCurrentStatus()(implicit t: Timeout): F[Either[String, CounterStatus]]
}

case class CounterServiceImpl(statusActor: ActorRef) extends CounterService[Future] with LazyLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def getCurrentStatus()(implicit t: Timeout): Future[Either[String, CounterStatus]] = {
    (statusActor ? WordCounterActor.RetrieveStatus)
      .mapTo[CounterStatus]
      .map(Right(_))
      .recover {
        case e: Exception => Left(e.getMessage)
      }
  }

}
