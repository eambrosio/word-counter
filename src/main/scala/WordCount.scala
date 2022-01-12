import CounterUtils._
import actor.WordCounterActor
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.stream.scaladsl.{Framing, Source, StreamConverters}
import akka.util.{ByteString, Timeout}
import com.typesafe.scalalogging.LazyLogging
import http.{CounterEndpoint, CounterServiceImpl}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MINUTES, SECONDS}

object WordCount extends App with LazyLogging {
  import model.EventProtocol._

  implicit val timeout: Timeout      = Timeout.durationToTimeout(FiniteDuration(1, MINUTES))
  implicit val as: ActorSystem       = ActorSystem("WordCount")
  implicit val session: SlickSession = SlickSession.forConfig("slick-postgres")

  val WindowDurationInSeconds: FiniteDuration =
    FiniteDuration(sys.env.getOrElse("WINDOW_DURATION", "5").toLong, SECONDS)

  val wordCounterActor                        = as.actorOf(WordCounterActor.props())
  private val counterService                  = CounterServiceImpl(wordCounterActor)
  private val counterEndpoint                 = CounterEndpoint(counterService)

  val stdinSource: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => System.in)

  startServer(counterEndpoint.routes)
  retrieveData(wordCounterActor)

  logger.info(s"Window duration: $WindowDurationInSeconds")

  stdinSource
    .via(lineExtractor)
    .filter(validLine)
    .via(parseJson)
    .groupBy(Int.MaxValue, event => event.event_type)
    .map(e => (e.event_type, e.data.split("\\\\w+").length.toLong))
    .groupedWithin(1000, WindowDurationInSeconds)
    .map(updateData(wordCounterActor))
    .map(_ => persistData(wordCounterActor))
    .mergeSubstreams
    .run()

}
