import CounterUtils.parseJson
import actor.WordCounterActor
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.IOResult
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.{ByteString, Timeout}
import com.typesafe.scalalogging.LazyLogging
import http.{CounterEndpoint, CounterServiceImpl}
import io.circe.Decoder
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MINUTES, SECONDS}
import scala.util.{Failure, Success}

object WordCount extends App with LazyLogging {
  import model.MyJsonProtocol._

  implicit val timeout: Timeout = Timeout.durationToTimeout(FiniteDuration(1, MINUTES))
  implicit val as: ActorSystem  = ActorSystem("WordCount")

  val wordCounterActor        = as.actorOf(WordCounterActor.props())
  private val counterService  = CounterServiceImpl(wordCounterActor)
  private val counterEndpoint = CounterEndpoint(counterService)

  startServer(counterEndpoint.route)

  val stdinSource: Source[ByteString, Future[IOResult]] = StreamConverters.fromInputStream(() => System.in)
  val stdoutSink: Sink[ByteString, Future[IOResult]]    = StreamConverters.fromOutputStream(() => System.out)

  stdinSource
    .via(parseJson)
    .groupBy(Int.MaxValue, event => event.event_type)
    .map(e => (e.event_type, e.data.split("\\\\w+").length.toLong))
    .groupedWithin(1000, FiniteDuration(5, SECONDS))
    .map {
      data =>
        println(data)
        data
    }
//    .map(data => wordCounterActor ! Count((data.head._1, data.map(_._2).sum)))
    .map(data => wordCounterActor ! WordCounterActor.Count(data))
    .mergeSubstreams
//    .runForeach(s => println(s"the number of words was: $s"))
    .run()

//  startHttpServer(endpoint.route)
//      Source(List("""{"id":"1"}""","""{"iddd":"2"}""","""{"id":"3"}\n"""))
//        .map(ByteString.fromString)
////         .single(ByteString.fromString("""{"id":"1"}"""))
//    .via(parseJson)
//    .groupedWithin(1000, FiniteDuration(5, SECONDS))
//    .map(_.size)
//    .runForeach(s => println(s"the size was: $s"))

  private def startServer(routes: Route): Unit = {
    val futureBinding = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex)      =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        as.terminate()
    }
  }

}
