package actor

import actor.WordCounterActor._
import akka.actor.{Actor, ActorLogging, PoisonPill, Props, Stash}
import akka.pattern.pipe
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import http.SnapshotService
import model.CounterStatus

import scala.concurrent.{ExecutionContext, Future}

case class WordCounterActor(statusService: SnapshotService[Future])(implicit
    ec: ExecutionContext,
    t: Timeout
) extends Actor
    with Stash
    with ActorLogging {

  def running(counterStatus: CounterStatus): Receive = {

    case UpdateCount(data) =>
      val (k, v)    = (data.head._1, data.map(_._2).sum)
      val newStatus = counterStatus.update(k, v)
      context become running(newStatus)

    case RetrieveStatus    =>
      sender() ! RetrieveStatusResult(counterStatus)

    case PersistStatus     =>
      statusService.persistStatus(counterStatus)
      log.info("data persisted")

  }

  override def receive: Receive = initStatus

  def initStatus: Receive = {
    statusService.retrieveStatus() pipeTo self
    //        .mapTo[Either[String,CounterStatus]]

    {
      case Right(status) if status.asInstanceOf[CounterStatus].data.isEmpty  =>
        log.info("no previous status. Starting the actor with empty status...")
        context become running(status.asInstanceOf[CounterStatus])
        log.info("unstashing messages...")
        unstashAll()
      case Right(status) if status.asInstanceOf[CounterStatus].data.nonEmpty =>
        log.info("previous status exists. Starting the actor with previous status...")
        context become running(status.asInstanceOf[CounterStatus])
        log.info("unstashing messages...")
        unstashAll()
      case Left(msg)                                                         =>
        log.error(msg.asInstanceOf[String])
        log.error("No persistence")
        self ! PoisonPill

      case msg                                                               =>
        log.info(s"Staging message $msg")
        stash()
    }
  }

}

object WordCounterActor {
  case class UpdateCount(data: Seq[(String, Long)])
  case object PersistStatus
  case object RetrieveStatus
  case object InitStatus
  case object Running
  case class RetrieveStatusResult(status: CounterStatus)

  def props(statusService: SnapshotService[Future])(implicit
      session: SlickSession,
      ec: ExecutionContext,
      t: Timeout
  ): Props = Props(new WordCounterActor(statusService))

}
