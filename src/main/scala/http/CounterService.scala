package http

import actor.WordCounterActor
import actor.WordCounterActor.RetrieveStatusResult
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import model.CounterStatus

import scala.concurrent.{ExecutionContext, Future}

trait CounterService[F[_]] {
  def getStatus(): F[Either[String, CounterStatus]]
}

case class CounterActorImpl(actor: ActorRef)(implicit ec: ExecutionContext, t: Timeout) extends CounterService[Future] with LazyLogging {

  override def getStatus() : Future[Either[String, CounterStatus]] = {
    logger.info("retrieving status from service")
//    Thread.sleep(5000)
    (actor ? WordCounterActor.RetrieveStatus)
      .mapTo[RetrieveStatusResult]
      .map(result => Right(result.status))
      .recover {
        case e: Exception =>
          Left(e.getMessage)
      }
  }

}
