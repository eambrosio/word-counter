import WordCounterActor.Count
import akka.actor.{Actor, Props}

class WordCounterActor extends Actor {

  override def receive: Receive = running(Map[String, Long]())

  def running(counterStatus: Map[String, Long]): Receive = {
    case Count((k, v)) =>
          val newStatus = updateStatus(counterStatus, k, v)
          println(newStatus)
          context become running(newStatus)

  }

  private def updateStatus(counterStatus: Map[String, Long], k: String, v: Long): Map[String, Long] =
    counterStatus.updated(k, counterStatus.getOrElse(k, 0L) + v)

}

object WordCounterActor {
  case class Count(data: (String, Long))

  def props(): Props = Props(new WordCounterActor)
}
