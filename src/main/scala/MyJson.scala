import spray.json.DefaultJsonProtocol

case class MyJson(event_type: String, data: String)

object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val myJsonFormat = jsonFormat2(MyJson)
}
