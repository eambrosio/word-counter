import CounterUtils.parseJson
import WordCounterActor.Count
import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.stream.scaladsl.{Sink, Source, StreamConverters}
import akka.util.ByteString
import io.circe.Decoder
import io.circe.generic.auto._

import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, SECONDS}

object WordCount extends App {
  import MyJsonProtocol._

  implicit val as: ActorSystem = ActorSystem("WordCount")

  val stdinSource: Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => System.in)

  val stdoutSink: Sink[ByteString, Future[IOResult]] =
    StreamConverters.fromOutputStream(() => System.out)

  val wordCounterActor = as.actorOf(WordCounterActor.props())

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
    .map(data => wordCounterActor ! Count((data.head._1, data.map(_._2).sum)))
    .mergeSubstreams
//    .runForeach(s => println(s"the number of words was: $s"))
    .run()

//      Source(List("""{"id":"1"}""","""{"iddd":"2"}""","""{"id":"3"}\n"""))
//        .map(ByteString.fromString)
////         .single(ByteString.fromString("""{"id":"1"}"""))
//    .via(parseJson)
//    .groupedWithin(1000, FiniteDuration(5, SECONDS))
//    .map(_.size)
//    .runForeach(s => println(s"the size was: $s"))

}
