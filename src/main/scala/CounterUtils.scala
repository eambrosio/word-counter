import akka.NotUsed
import akka.stream.scaladsl.{Flow, JsonFraming}
import akka.stream.{ActorAttributes, Supervision}
import akka.util.ByteString
import spray.json._

object CounterUtils {

  import MyJsonProtocol._

  val parseJson: Flow[ByteString, MyJson, NotUsed] =
    JsonFraming
      .objectScanner(Int.MaxValue)
      .map(_.utf8String.parseJson.convertTo[MyJson])
      .withAttributes(ActorAttributes.supervisionStrategy(Supervision.resumingDecider))
  //      .map(JsonReader.select("$.rows[*]"))



  def capitaliseByteString(byteString: ByteString): ByteString =
    ByteString(byteString.utf8String.toUpperCase)



}
