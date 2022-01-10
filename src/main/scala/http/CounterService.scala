package http

import actor.WordCounterActor
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future

trait CounterService[F[_]] {
  def getCurrentStatus()(implicit t: Timeout): F[Either[String, Map[String, Long]]]
}

case class CounterServiceImpl(statusActor: ActorRef) extends CounterService[Future] with LazyLogging {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def getCurrentStatus()(implicit t: Timeout): Future[Either[String, Map[String, Long]]] = {
    (statusActor ? WordCounterActor.RetrieveStatus)
      .mapTo[Map[String, Long]]
      .map(Right(_))
      .recover {
        case e: Exception => Left(e.getMessage)
      }
  }

}
