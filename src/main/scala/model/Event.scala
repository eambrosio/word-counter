package model

import spray.json.DefaultJsonProtocol

case class Event(event_type: String, data: String, timestamp: Long)

object EventProtocol extends DefaultJsonProtocol {
  implicit val eventFormat = jsonFormat3(Event)
}
