import akka.NotUsed
import akka.stream.scaladsl.{Flow, JsonFraming}
import akka.stream.{ActorAttributes, Supervision}
import akka.util.ByteString
import spray.json._

object CounterUtils {

  def parseJson[T](implicit jr: JsonReader[T]): Flow[ByteString, T, NotUsed] =
    JsonFraming
      .objectScanner(Int.MaxValue)
      .map(_.utf8String.parseJson.convertTo[T])
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))

  //      .map(JsonReader.select("$.rows[*]"))

  def capitaliseByteString(byteString: ByteString): ByteString =
    ByteString(byteString.utf8String.toUpperCase)

}
