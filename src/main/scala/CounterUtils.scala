import WordCount.{as, session}
import actor.WordCounterActor
import actor.WordCounterActor.InitStatus
import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Flow, Framing, JsonFraming}
import akka.stream.{ActorAttributes, Attributes, Supervision}
import akka.util.ByteString
import com.typesafe.scalalogging.LazyLogging
import spray.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object CounterUtils extends LazyLogging {

  def validLine(data: ByteString): Boolean =
    data.startsWith(ByteString("{")) && data.endsWith(ByteString("}"))

  def lineExtractor: Flow[ByteString, ByteString, NotUsed]                   =
    Framing.delimiter(ByteString("\n"), Int.MaxValue, true)

  def parseJson[T](implicit jr: JsonReader[T]): Flow[ByteString, T, NotUsed] =
    JsonFraming
      .objectScanner(Int.MaxValue)
      .map(_.utf8String.parseJson.convertTo[T])
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.restartingDecider))
      .log(name = "parser-log")
      .addAttributes(
        Attributes.logLevels(
          onElement = Attributes.LogLevels.Info,
          onFinish = Attributes.LogLevels.Info,
          onFailure = Attributes.LogLevels.Error
        )
      )

  def persistData(actor:ActorRef): Unit =
    actor ! WordCounterActor.PersistStatus

  def updateData(actor:ActorRef)(data: Seq[(String, Long)]): Unit =
    actor ! WordCounterActor.UpdateCount(data)

  def startServer(routes: Route): Unit = {
    val futureBinding = Http().newServerAt("0.0.0.0", 8000).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex)      =>
        logger.error("Failed to bind HTTP endpoint, terminating system", ex)
        as.terminate()
        as.registerOnTermination(() => session.close())
    }
  }

}
