package model

import play.api.libs.json._
import play.api.libs.functional.syntax._

import com.github.nscala_time.time.Imports._

import org.joda.time.format.ISODateTimeFormat
import org.joda.time.Instant

import configuration.Config
import utils.StringUtils.truncateToWordBoundary

object Eventbrite {

  trait EBObject

  sealed trait EBEventStatus

  sealed trait DisplayableEvent extends EBEventStatus

  case object Completed extends EBEventStatus
  case object Cancelled extends EBEventStatus
  case object SoldOut extends DisplayableEvent
  case object Live extends DisplayableEvent
  case object PreLive extends DisplayableEvent
  case object Draft extends EBEventStatus


  case class EBError(error: String, error_description: String, status_code: Int) extends Throwable with EBObject {
    override def getMessage: String = s"$status_code $error - $error_description"
  }

  case class EBResponse[T](pagination: EBPagination, data: Seq[T]) extends EBObject

  case class EBPagination(page_number: Int,
                          page_count: Int) extends EBObject {
    lazy val nextPageOpt = Some(page_number + 1).filter(_ <= page_count)
  }

  case class EBRichText(text: String, html: String) {
    def cleanHtml: String = {
      val stylePattern = "(?i)style=(\".*?\"|'.*?'|[^\"'][^\\s]*)".r
      val cleanStyle = stylePattern replaceAllIn(html, "")
      val clean = "(?i)<br>".r.replaceAllIn(cleanStyle, "")
      clean
    }

    lazy val blurb = truncateToWordBoundary(text, 200)
  }

  case class EBAddress(address_1: Option[String],
                       address_2: Option[String],
                       city: Option[String],
                       region: Option[String],
                       postal_code: Option[String],
                       country: Option[String]) extends EBObject

  case class EBVenue(address: Option[EBAddress], name: Option[String]) extends EBObject

  case class EBPricing(value: Int) extends EBObject {
    def priceFormat(priceInPence: Double) = {
      val priceInPounds = priceInPence.round / 100f
      if (priceInPounds.isWhole) f"£$priceInPounds%.0f" else f"£$priceInPounds%.2f"
    }

    lazy val formattedPrice = priceFormat(value)
    lazy val discountPrice = priceFormat(value * Config.discountMultiplier)
    lazy val savingPrice = priceFormat(value * (1 - Config.discountMultiplier))
  }


  /**
   * https://developer.eventbrite.com/docs/ticket-class-object/
   */
  case class EBTickets(name: String,
                       free: Boolean,
                       quantity_total: Int,
                       quantity_sold: Int,
                       cost: Option[EBPricing],
                       sales_end: Instant,
                       sales_start: Option[Instant],
                       hidden: Option[Boolean]) extends EBObject

  case class EBEvent(name: EBRichText,
                     description: Option[EBRichText],
                     url: String,
                     id: String,
                     start: DateTime,
                     created: Instant,
                     venue: EBVenue,
                     capacity: Int,
                     ticket_classes: Seq[EBTickets],
                     status: String) extends EBObject {

    lazy val eventAddressLine = venue.address.map { a =>
      Seq(a.address_1, a.address_2, a.city, a.region, a.postal_code).flatten.filter(_.nonEmpty)
    }.getOrElse(Nil).mkString(", ")

    def getStatus: EBEventStatus = {
      val isSoldOut = ticket_classes.map(_.quantity_sold).sum >= capacity
      val isTicketSalesStarted = ticket_classes.exists(_.sales_start.forall(_ <= Instant.now))


      status match {
        case "completed" | "started" | "ended" => Completed
        case "canceled" => Cancelled // American spelling
        case "live" if isSoldOut => SoldOut
        case "live" if isTicketSalesStarted => Live
        case "draft" => Draft
        case _ => PreLive
      }
    }

    lazy val memUrl = Config.membershipUrl + controllers.routes.Event.details(id)

    lazy val isNoTicketEvent = description.exists(_.html.contains("<!-- noTicketEvent -->"))

    val generalReleaseTicket = ticket_classes.find(_.hidden.forall(_ == false))
  }

  case class EBDiscount(code: String, quantity_available: Int, quantity_sold: Int) extends EBObject

  //https://developer.eventbrite.com/docs/order-object/
  case class EBOrder(id: String, first_name: String, email: String, costs: EBCosts, attendees: Seq[EBAttendee]) extends EBObject {
    val ticketCount = attendees.length
    val totalCost = costs.gross.value / 100f
  }

  case class EBCosts(gross: EBCost) extends EBObject

  case class EBCost(value: Int) extends EBObject

  case class EBAttendee(quantity: Int) extends EBObject
}

object EventbriteDeserializer {
  import Eventbrite._

  private def ebResponseReads[T](namespace: String)(implicit reads: Reads[Seq[T]]): Reads[EBResponse[T]] =
    ((JsPath \ "pagination").read[EBPagination] and
      (JsPath \ namespace).read[Seq[T]])(EBResponse[T] _)

  private def convertInstantText(utc: String): Instant =
    ISODateTimeFormat.dateTimeNoMillis.parseDateTime(utc).toInstant

  private def convertDateText(utc: String, timezone: String): DateTime = {
    val timeZone = DateTimeZone.forID(timezone)
    ISODateTimeFormat.dateTimeNoMillis.parseDateTime(utc).withZone(timeZone)
  }

  implicit val instant: Reads[Instant] = JsPath.read[String].map(convertInstantText)

  implicit val readsEbDate: Reads[DateTime] = (
    (JsPath \ "utc").read[String] and
      (JsPath \ "timezone").read[String]
    )(convertDateText _)

  implicit val ebError = Json.reads[EBError]
  implicit val ebLocation = Json.reads[EBAddress]
  implicit val ebVenue = Json.reads[EBVenue]

  implicit val ebRichText: Reads[EBRichText] = (
      (JsPath \ "text").readNullable[String].map(_.getOrElse("")) and
        (JsPath \ "html").readNullable[String].map(_.getOrElse(""))
    )(EBRichText.apply _)

  implicit val ebPricingReads = Json.reads[EBPricing]
  implicit val ebTicketsReads = Json.reads[EBTickets]
  implicit val ebEventReads = Json.reads[EBEvent]
  implicit val ebDiscountReads = Json.reads[EBDiscount]

  implicit val ebPaginationReads = Json.reads[EBPagination]
  implicit val ebEventsReads = ebResponseReads[EBEvent]("events")
  implicit val ebDiscountsReads = ebResponseReads[EBDiscount]("discounts")

  implicit val ebCostReads = Json.reads[EBCost]
  implicit val ebCostsReads = Json.reads[EBCosts]
  implicit val ebAttendeeReads = Json.reads[EBAttendee]
  implicit val ebOrderReads = Json.reads[EBOrder]
}
