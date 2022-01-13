package actor

import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.util.Timeout
import http.{SnapshotService, SnapshotServicePostgresImpl}
import model.CounterStatus
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait WordCounterActorData extends MockitoSugar {

  implicit val session: SlickSession = mock[SlickSession]
  implicit val timeout: Timeout      = Timeout.durationToTimeout(FiniteDuration(5, SECONDS))

  val emptySnapshotService: SnapshotService[Future] = new SnapshotServicePostgresImpl() {

    override def retrieveStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus.empty()))

  }

  val nonEmptySnapshotService: SnapshotService[Future] = new SnapshotServicePostgresImpl() {

    override def retrieveStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Right(CounterStatus(Map("foo" -> 1))))

  }

  val failingSnapshotService: SnapshotService[Future] = new SnapshotServicePostgresImpl() {

    override def retrieveStatus(): Future[Either[String, CounterStatus]] =
      Future.successful(Left("error"))

  }

}
