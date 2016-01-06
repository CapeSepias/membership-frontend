package services

import actions.{AnyMemberTierRequest, MemberRequest}
import com.github.nscala_time.time.Imports._
import com.gu.contentapi.client.parser.JsonParser
import com.gu.identity.play.{AccessCredentials, AuthenticatedIdUser, IdMinimalUser}
import com.gu.salesforce.Tier.Friend
import com.gu.salesforce._
import model.Eventbrite.EBAccessCode
import model.EventbriteTestObjects._
import model.{ContentDestination, EventDestination}
import org.scalatest.concurrent.ScalaFutures
import org.specs2.mock.Mockito
import play.api.mvc.Security.AuthenticatedRequest
import play.api.test.{FakeApplication, FakeRequest, PlaySpecification}
import play.api.{Application, GlobalSettings}
import utils.Resource

import scala.concurrent.Future

class DestinationServiceTest extends PlaySpecification with Mockito with ScalaFutures {

  "DestinationService" should {

    val fakeApplicationWithGlobal = FakeApplication(withGlobal = Some(new GlobalSettings() {
      override def onStart(app: Application) {}
    }))

    val fakeMemberService = mock[MemberService]

    object DestinationServiceTest extends DestinationService {
      override val contentApiService = mock[GuardianContentService]
      override val eventbriteService = mock[EventbriteCollectiveServices]
      override def memberService(request: AnyMemberTierRequest[_]): MemberService = fakeMemberService
    }

    val destinationService = DestinationServiceTest

    def createRequestWithSession(newSessions: (String, String)*) = {
      val testMember = Contact(ContactDetails("id", Some("fn"), "ln", "email", new DateTime(), "contactId", "accountId"), FreeTierMember(Friend), NoPayment)
      val fakeRequest = FakeRequest().withSession(newSessions: _*)
      val minimalUser: IdMinimalUser = IdMinimalUser("123", None)
      MemberRequest(testMember, new AuthenticatedRequest(AuthenticatedIdUser(AccessCredentials.Cookies("foo", "bar"), minimalUser), fakeRequest))

    }

    "should return a content destination url if join-referrer is in the request session" in {
      running(fakeApplicationWithGlobal) {

        val request = createRequestWithSession("join-referrer" -> "http://www.theguardian.com/membership/2015/apr/17/guardian-live-diversity-in-the-arts")

        //mock the Content API response
        val item = JsonParser.parseItem(Resource.get("model/content.api/item.json"))
        destinationService.contentApiService.contentItemQuery("/membership/2015/apr/17/guardian-live-diversity-in-the-arts") returns Future.successful(item)

        //call the method under test
        val futureResult = destinationService.contentDestinationFor(request)

        //verify eventDestinationFor returns a valid content destination
        whenReady(futureResult) { contentDestinationOpt =>
          contentDestinationOpt.get.item.content.id mustEqual "membership/2015/apr/17/guardian-live-diversity-in-the-arts"
        }
      }
    }

    "should return an event destination url if preJoinReturnUrl is in the request session" in {
      running(fakeApplicationWithGlobal) {

        val request = createRequestWithSession("preJoinReturnUrl" -> "/event/0123456/buy")


        //mock the Eventbrite response and discount creation
        val event = TestRichEvent(eventWithName().copy(id = "0123456"))
        destinationService.eventbriteService.getBookableEvent("0123456") returns Some(event)
        fakeMemberService.createEBCode(request.member, event) returns Future.successful(Some(EBAccessCode("some-discount-code", 2)))

        //call the method under test
        val futureResult = destinationService.eventDestinationFor(request)

        //verify eventDestinationFor returns a valid event destination
        whenReady(futureResult) { eventDestinationOpt =>
          eventDestinationOpt.get.event.id mustEqual "0123456"
        }
      }
    }

    "should return either content or event destination if both are supplied" in {
      running(fakeApplicationWithGlobal) {

        val request = createRequestWithSession("join-referrer" -> "http://www.theguardian.com/membership/2015/apr/17/guardian-live-diversity-in-the-arts", "preJoinReturnUrl" -> "/event/0123456/buy")

        //mock the Content API response
        val item = JsonParser.parseItem(Resource.get("model/content.api/item.json"))
        destinationService.contentApiService.contentItemQuery("/membership/2015/apr/17/guardian-live-diversity-in-the-arts") returns Future.successful(item)

        //mock the Eventbrite response and discount creation
        val event = TestRichEvent(eventWithName().copy(id = "0123456"))
        destinationService.eventbriteService.getBookableEvent("0123456") returns Some(event)
        fakeMemberService.createEBCode(request.member, event) returns Future.successful(Some(EBAccessCode("some-discount-code", 2)))

        //call the method under test with the request
        val futureResult = destinationService.returnDestinationFor(request)

        //verify returnForDestination returns a valid destination
        whenReady(futureResult) { destinationOpt =>
          val destination = destinationOpt.get
          destination match {
            case eventDestination: EventDestination => eventDestination.event.id mustEqual "0123456"
            case contentDestination: ContentDestination => contentDestination.item.content.id mustEqual "membership/2015/apr/17/guardian-live-diversity-in-the-arts"
          }
          val validDestination = destination.isInstanceOf[ContentDestination] || destination.isInstanceOf[EventDestination]
          validDestination must_== true
        }
      }
    }
  }
}
