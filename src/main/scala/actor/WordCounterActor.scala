package actor

import actor.WordCounterActor.{AddCount, DataPersisted, PersistData, RetrieveStatus}
import akka.actor.{Actor, Props}
import akka.stream.alpakka.slick.scaladsl.SlickSession
import com.typesafe.scalalogging.LazyLogging
import model.CounterStatus
import spray.json.enrichAny

class WordCounterActor()(implicit session: SlickSession) extends Actor with LazyLogging {
  import session.profile.api._
  import CounterStatus._

  override def receive: Receive = running(CounterStatus.empty())

  def running(counterStatus: CounterStatus): Receive = {
    case AddCount(data)      =>
      val (k, v)    = (data.head._1, data.map(_._2).sum)
      val newStatus = counterStatus.update(k, v)
      println(newStatus)
      self ! PersistData(newStatus)
      context become running(newStatus)

    case RetrieveStatus      =>
      sender() ! counterStatus

    case PersistData(status) =>
      session.db.run(sqlu"INSERT INTO counter_status(status) values(${status.data.toJson.compactPrint})")
      sender() ! DataPersisted

    case DataPersisted      =>
      logger.info("data persisted")

  }

}

object WordCounterActor {
  case class AddCount(data: Seq[(String, Long)])
  case class PersistData(status: CounterStatus)
  case object RetrieveStatus
  case object DataPersisted

  def props()(implicit session: SlickSession): Props = Props(new WordCounterActor)
}
