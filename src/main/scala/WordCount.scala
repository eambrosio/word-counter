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

  val wordCounterActor                                  = as.actorOf(WordCounterActor.props())
  val stdinSource: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => System.in)
  private val counterService                            = CounterServiceImpl(wordCounterActor)
  private val counterEndpoint                           = CounterEndpoint(counterService)

  startServer(counterEndpoint.route)
  retrieveData

  stdinSource
    .via(lineExtractor)
    .filter(validLine)
    .via(parseJson)
    .groupBy(Int.MaxValue, event => event.event_type)
    .map(e => (e.event_type, e.data.split("\\\\w+").length.toLong))
    .groupedWithin(1000, FiniteDuration(5, SECONDS))
    .map(updateData)
    .map(_ => persistData)
    .mergeSubstreams
    .run()


}
