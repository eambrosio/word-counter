package http

import akka.actor.ActorRef
import akka.util.Timeout
import model.CounterStatus
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait CounterEndpointData extends MockitoSugar {
  implicit val timeout: Timeout = Timeout.durationToTimeout(FiniteDuration(5, SECONDS))
  val mockActor                 = mock[ActorRef]

  val emptyStatusService: CounterService[Future] = new CounterServiceImpl(mockActor) {

    override def getCurrentStatus()(implicit t: Timeout): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus.empty()))

  }

  val nonEmptyStatusService: CounterService[Future] = new CounterServiceImpl(mockActor) {

    val status: Map[String, Long] = Map("foo" -> 3, "bar" -> 1)

    override def getCurrentStatus()(implicit t: Timeout): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus(status)))

  }

  val failingStatusService: CounterService[Future] = new CounterServiceImpl(mockActor) {

    override def getCurrentStatus()(implicit t: Timeout): Future[Either[String, CounterStatus]] =
      Future.successful(Left("unexpected error"))

  }

}
