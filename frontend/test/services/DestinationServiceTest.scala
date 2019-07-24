package services

import com.github.nscala_time.time.Imports._
import com.gu.contentapi.client.model.v1.{Content, ItemResponse}
import com.gu.i18n.Currency.GBP
import com.gu.memsub.Benefit._
import com.gu.memsub.BillingPeriod.Month
import com.gu.memsub.Subscription.{ProductRatePlanChargeId, ProductRatePlanId, RatePlanId, SubscriptionRatePlanChargeId}
import com.gu.memsub._
import com.gu.memsub.subsv2.{Subscription, _}
import com.gu.salesforce._
import model.Eventbrite.EBAccessCode
import model.EventbriteTestObjects._
import model.{ContentDestination, EventDestination}
import org.joda.time.LocalDate
import org.specs2.mutable.Specification
import play.api.mvc.Session

import scalaz.Id._

class DestinationServiceTest extends Specification {
  "DestinationService" should {

    type OldId[+X] = X

    val destinationService = new DestinationService[OldId](
      getBookableEvent = _ => Some(TestRichEvent(eventWithName().copy(id = "0123456"))),
      capiItemQuery = _ => DestinationServiceTest.createContentItem,
      createCode = (_, _) => Some(EBAccessCode("some-discount-code", 2))
    )

    def createRequestWithSession(newSessions: (String, String)*) = {

      val testMember = Contact(
        identityId= Some("id"),
        regNumber= None,
        title = None,
        firstName= Some("fn"),
        lastName= "ln",
        joinDate= new DateTime(),
        salesforceContactId= "contactId",
        salesforceAccountId= "accountId",
        mailingStreet= None,
        mailingCity= None,
        mailingState= None,
        mailingPostcode= None,
        mailingCountry= None,
        recordTypeId = None
      )
      val partnerCharge: PaidCharge[Partner.type, Month.type] = PaidCharge[Partner.type, Month.type](Partner, Month, PricingSummary(Map(GBP -> Price(0.1f, GBP))), ProductRatePlanChargeId("prpcId"),SubscriptionRatePlanChargeId("srpcid"))
      val testSub: Subscription[SubscriptionPlan.Member] = new Subscription[SubscriptionPlan.Partner](
        id = com.gu.memsub.Subscription.Id(""),
        name = com.gu.memsub.Subscription.Name(""),
        accountId = com.gu.memsub.Subscription.AccountId(""),
        startDate = new LocalDate("2015-01-01"),
        termStartDate = new LocalDate("2015-01-01"),
        acceptanceDate = new LocalDate("2015-01-01"),
        termEndDate = new LocalDate("2016-01-01"),
        promoCode = None,
        casActivationDate = None,
        isCancelled = false,
        plans = CovariantNonEmptyList(new PaidSubscriptionPlan[Product.Membership, PaidCharge[Partner.type, Month.type]](
          id = RatePlanId(""), productRatePlanId = ProductRatePlanId(""), name = "name", product = Product.Membership, description = "", features = Nil,
          charges = partnerCharge,
          chargedThrough = None, start = new LocalDate("2015-01-01"), end = new LocalDate("2099-01-01"), productName = ""), Nil),
        hasPendingFreePlan = false,
        readerType = ReaderType.Direct,
        autoRenew = true
      )

      val testSubscriber: Subscriber.Member = Subscriber(testSub, testMember)
      (Session(newSessions.toMap), testSubscriber)
    }

    "should return a content destination url if join-referrer is in the request session" in {
      val (request, _) = createRequestWithSession("join-referrer" -> "http://www.theguardian.com/membership/2015/apr/17/guardian-live-diversity-in-the-arts")
      val result = destinationService.contentDestinationFor(request)
      result.get.item.content.id mustEqual "membership/2015/apr/17/guardian-live-diversity-in-the-arts"
    }

    "should return an event destination url if preJoinReturnUrl is in the request session" in {
      val (request, member) = createRequestWithSession("preJoinReturnUrl" -> "/event/0123456/buy")
      val result = destinationService.eventDestinationFor(request, member)
      result.get.event.id mustEqual "0123456"
    }

    "should return either content or event destination if both are supplied" in {
      val (request, member) = createRequestWithSession("join-referrer" -> "http://www.theguardian.com/membership/2015/apr/17/guardian-live-diversity-in-the-arts", "preJoinReturnUrl" -> "/event/0123456/buy")

      destinationService.returnDestinationFor(request, member).get match {
        case EventDestination(event, _) => event.id mustEqual "0123456"
        case ContentDestination(item) => item.content.id mustEqual "membership/2015/apr/17/guardian-live-diversity-in-the-arts"
      }
    }
  }
}

object DestinationServiceTest {
  def createContentItem = {
    ItemResponse("", "", content = Some(Content(
      "membership/2015/apr/17/guardian-live-diversity-in-the-arts", webTitle = "", webUrl = "", apiUrl = ""
    )))
  }
}
