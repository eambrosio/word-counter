package actor

import actor.WordCounterActor.{RetrieveStatus, RetrieveStatusResult, UpdateCount}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import model.CounterStatus
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class WordCounterActorSpec
    extends TestKit(ActorSystem("WordCounterActorSpec"))
    with AnyFlatSpecLike
    with Matchers
    with ImplicitSender
    with WordCounterActorData
    with GivenWhenThen
    with BeforeAndAfterAll {

  it should "return an empty status" in {
    Given("a WordCounterActor")
    And("an empty status returned by the service")
    val probe        = TestProbe()
    val counterActor = system.actorOf(WordCounterActor.props(emptySnapshotService))

    When("we send a RetrieveStatus message")
    probe.send(counterActor, RetrieveStatus)

    Then("the actor returns the message RetrieveStatusResult with an empty status")
    probe.expectMsg(5.seconds , RetrieveStatusResult(CounterStatus.empty()))
  }

  it should "return the previous status" in {
    Given("a WordCounterActor")
    And("an empty status returned by the service")
    val probe        = TestProbe()
    val counterActor = system.actorOf(WordCounterActor.props(nonEmptySnapshotService))

    When("we send a RetrieveStatus message")
    probe.send(counterActor, RetrieveStatus)

    Then("the actor returns the message RetrieveStatusResult with an empty status")
    probe.expectMsg(RetrieveStatusResult(CounterStatus(Map("foo" -> 1))))
  }

  it should "kill the actor" in {
    Given("a WordCounterActor")
    And("an empty status returned by the service")
    val probe        = TestProbe()
    val counterActor = system.actorOf(WordCounterActor.props(failingSnapshotService))

    When("we send a RetrieveStatus message")
    probe watch counterActor

    Then("the actor returns the message RetrieveStatusResult with an empty status")
    probe.expectTerminated(counterActor)
  }

  it should "return an updated status" in {
    Given("a WordCounterActor")
    And("an empty status returned by the service")
    val probe        = TestProbe()
    val counterActor = system.actorOf(WordCounterActor.props(nonEmptySnapshotService))

    When("we send a RetrieveStatus message")
    probe.send(counterActor, UpdateCount(List(("bar", 2))))
    probe.send(counterActor, RetrieveStatus)

    Then("the actor returns the message RetrieveStatusResult with an empty status")
    probe.expectMsg(RetrieveStatusResult(CounterStatus(Map("foo" -> 1, "bar" -> 2))))
  }

  override protected def afterAll(): Unit =
    shutdown(system)

}
