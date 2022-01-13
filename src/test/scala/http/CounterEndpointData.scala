package http

import akka.actor.ActorRef
import akka.util.Timeout
import model.CounterStatus
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait CounterEndpointData extends MockitoSugar {
  implicit val timeout: Timeout = Timeout.durationToTimeout(FiniteDuration(5, SECONDS))
  val mockActor                 = mock[ActorRef]

  val emptyCounterService: CounterService[Future] = new CounterActorImpl(mockActor) {

    override def getStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus.empty()))

  }

  val nonEmptyCounterService: CounterService[Future] = new CounterActorImpl(mockActor) {

    val status: Map[String, Long] = Map("foo" -> 3, "bar" -> 1)

    override def getStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus(status)))

  }

  val failingCounterService: CounterService[Future] = new CounterActorImpl(mockActor) {

    override def getStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Left("unexpected error"))

  }

}
