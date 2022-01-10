package actor

import actor.WordCounterActor.{Count, RetrieveStatus}
import akka.actor.{Actor, Props}

class WordCounterActor extends Actor {

  override def receive: Receive = running(Map[String, Long]())

  def running(counterStatus: Map[String, Long]): Receive = {
    case Count(data)    =>
      val (k, v)    = (data.head._1, data.map(_._2).sum)
      val newStatus = updateStatus(counterStatus, k, v)
      println(newStatus)
      context become running(newStatus)

    case RetrieveStatus =>
      sender() ! counterStatus
  }

  private def updateStatus(counterStatus: Map[String, Long], k: String, v: Long): Map[String, Long] =
    counterStatus.updated(k, counterStatus.getOrElse(k, 0L) + v)

}

object WordCounterActor {
  case class Count(data: Seq[(String, Long)])
  case object RetrieveStatus

  def props(): Props = Props(new WordCounterActor)
}
