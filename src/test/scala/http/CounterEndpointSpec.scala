package http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import model.CounterStatus
import org.scalatest.GivenWhenThen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CounterEndpointSpec
    extends AnyFlatSpec
    with Matchers
    with CounterEndpointData
    with GivenWhenThen
    with ScalatestRouteTest {

  behavior of "CounterEndpoint"

  it should "return an empty json when no previous status found" in {
    Given("a counter endpoint")
    And("a service which returns no previous status")
    val endpoint = CounterEndpoint(emptyStatusService)

    When("we perform a GET request to /counter")

    Get("/counter") ~>
      endpoint.routes ~>
      check {
        Then("the response contains a json with no data")
        responseAs[CounterStatus].data should be(empty)
      }
  }

  it should "return a json containing the same data as in the previous status found" in {
    Given("a counter endpoint")
    And("a service which returns a previous status")
    val endpoint = CounterEndpoint(nonEmptyStatusService)

    When("we perform a GET request to /counter")

    Get("/counter") ~>
      endpoint.routes ~>
      check {
        Then("the response contains a json with the expected data")
        responseAs[CounterStatus].data should contain theSameElementsAs Map("foo" -> 3, "bar" -> 1)
      }
  }

  it should "retur d" in {
    Given("a counter endpoint")
    And("a service which returns a previous status")
    val endpoint = CounterEndpoint(failingStatusService)

    When("we perform a GET request to /counter")

    Get("/counter") ~>
      endpoint.routes ~>
      check {
        status shouldEqual StatusCodes.BadRequest
      }
  }

  it should "fail when invalid path is requested" in {
    Given("a counter endpoint")
    And("a service which returns a previous status")
    val endpoint = CounterEndpoint(nonEmptyStatusService)

    When("we perform a GET request to /invalid-path")

    Get("/invalid-path") ~>
      endpoint.routes ~>
      check {
        Then("the invalid path is unhandled")
        handled shouldBe false
      }
  }
}
