package actor

import actor.WordCounterActor.{AddCount, RetrieveStatus}
import akka.actor.{Actor, Props}
import model.CounterStatus

class WordCounterActor extends Actor {

  override def receive: Receive = running(CounterStatus.empty())

  def running(counterStatus: CounterStatus): Receive = {
    case AddCount(data) =>
      val (k, v)    = (data.head._1, data.map(_._2).sum)
      val newStatus = counterStatus.update(k, v)
      println(newStatus)
      context become running(newStatus)

    case RetrieveStatus =>
      sender() ! counterStatus
  }

}

object WordCounterActor {
  case class AddCount(data: Seq[(String, Long)])
  case object RetrieveStatus

  def props(): Props = Props(new WordCounterActor)
}
