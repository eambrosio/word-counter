package model

import spray.json.DefaultJsonProtocol

case class CounterStatus(data: Map[String, Long]) {
  def update(k: String, v: Long): CounterStatus = copy(data.updated(k, data.getOrElse(k, 0L) + v))
  def size: Int                                 = data.size
}

object CounterStatus extends DefaultJsonProtocol {
  def empty(): CounterStatus = CounterStatus(Map[String, Long]())

  implicit val counterStatusFormat = jsonFormat1(CounterStatus.apply)
}
