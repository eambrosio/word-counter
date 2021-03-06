package http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import scala.concurrent.{ExecutionContextExecutor, Future}

case class CounterEndpoint(counterService: CounterService[Future])(implicit t: Timeout) extends EndpointDirectives {

  val routes: Route = getCounterStatus

  def getCounterStatus: Route =
    path("counter") {
      get {
        responseFromFuture(counterService.getStatus())
      }
    }

}
