import CounterUtils.parseJson
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.{ActorAttributes, IOResult, Supervision}
import akka.stream.alpakka.json.scaladsl.JsonReader
import akka.stream.scaladsl.{Flow, JsonFraming, Sink, Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.Future
import io.circe.Decoder
import io.circe.generic.auto._
import spray.json.DefaultJsonProtocol
import spray.json._

import scala.concurrent.duration.{Duration, DurationInt, FiniteDuration, SECONDS}

object WordCount extends App {
  import MyJsonProtocol._

  implicit val as: ActorSystem = ActorSystem("WordCount")

//  val results = Source
//    .single(ByteString.fromString("baseDocument"))
//    .via(JsonReader.select("$.rows[*].doc"))
//    .runWith(Sink.seq)

  val stdinSource: Source[ByteString, Future[IOResult]] =
    StreamConverters.fromInputStream(() => System.in)
  val stdoutSink: Sink[ByteString, Future[IOResult]] =
    StreamConverters.fromOutputStream(() => System.out)


  stdinSource
    .via(parseJson)
    .groupedWithin(1000, FiniteDuration(5, SECONDS))
    .map(_.size)
    .runForeach(s => println(s"the size was: $s"))


//      Source(List("""{"id":"1"}""","""{"iddd":"2"}""","""{"id":"3"}\n"""))
//        .map(ByteString.fromString)
////         .single(ByteString.fromString("""{"id":"1"}"""))
//    .via(parseJson)
//    .groupedWithin(1000, FiniteDuration(5, SECONDS))
//    .map(_.size)
//    .runForeach(s => println(s"the size was: $s"))

}
