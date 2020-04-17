package views.support

import java.net.URLEncoder
import configuration.{Config, Social => SocialConfig}
import model.RichEvent.RichEvent

sealed trait Social
case class Facebook(url: String, title: String = "") extends Social
case class LinkedIn(url: String) extends Social
case class GooglePlus(url: String) extends Social
case class Twitter(message: String) extends Social
case class Email(subject: String, message: String) extends Social

object Social {
  def encodeEmail(str: String) = URLEncoder.encode(str, "UTF-8").replaceAll("\\+", "%20")
  def encode(str: String) = URLEncoder.encode(str, "UTF-8")

  def link(s: Social) = s match {
    case Twitter(message) => s"https://twitter.com/intent/tweet?text=${Social.encode(message)}&amp;related=${SocialConfig.twitterUsername}"
    case Email(subject, body) => s"mailto:?subject=${Social.encodeEmail(subject)}&amp;body=${Social.encodeEmail(body)}"
    case Facebook(url, title) => s"https://www.facebook.com/sharer/sharer.php?u=${Social.encode(url)}&t=${Social.encode(title)}"
    case LinkedIn(url) => s"https://www.linkedin.com/shareArticle?url=${Social.encode(url)}"
    case GooglePlus(url) => s"https://plus.google.com/share?url=${Social.encode(url)}"
  }

  def metricAction(s: Social) = s match {
    case e: Email => "email"
    case t: Twitter => "twitter"
    case f: Facebook => "facebook"
    case l: LinkedIn => "linkedin"
    case g: GooglePlus => "googleplus"
  }

  def tooltip(s: Social) = s match {
    case e: Email => "Share via Email"
    case t: Twitter => "Share on Twitter"
    case f: Facebook => "Share on Facebook"
    case l: LinkedIn => "Share on LinkedIn"
    case g: GooglePlus => "Share on Google+"
  }

  def icon(s: Social) = s match {
    case e: Email => "share-email"
    case t: Twitter => "share-twitter"
    case f: Facebook => "share-facebook"
    case l: LinkedIn => "share-linkedin"
    case g: GooglePlus => "share-gplus"
  }

  def eventDetail(event: RichEvent): Set[Social] = Set(
    Email(event.underlying.ebEvent.name.text, s"The Guardian is coming to life through Guardian Live events like this one. Shall we go?\n\n${event.underlying.ebEvent.name.text}\n${event.underlying.ebEvent.memUrl}"),
    Twitter(s"${event.underlying.ebEvent.name.text} ${event.underlying.ebEvent.memUrl} ${event.metadata.socialHashtag.mkString}"),
    Facebook(event.underlying.ebEvent.memUrl)
  )

  def eventThankyou(event: RichEvent): Set[Social] = Set(
    Email(s"I'm going to ${event.underlying.ebEvent.name.text}!", s"I've just booked my ticket for ${event.underlying.ebEvent.name.text}. Come along too!\n\n${event.underlying.ebEvent.memUrl}"),
    Twitter(s"I'm going to: ${event.underlying.ebEvent.name.text} ${event.underlying.ebEvent.memUrl} ${event.metadata.socialHashtag.mkString}"),
    Facebook(event.underlying.ebEvent.memUrl)
  )

  val joinThankyou: Set[Social] = Set(
    Email("I'm the newest Guardian member", s"I'm the newest Guardian member ${Config.membershipSupporterUrl}"),
    Twitter(s"I'm the newest Guardian member ${Config.membershipSupporterUrl} ${SocialConfig.twitterHashtag}"),
    Facebook(Config.membershipSupporterUrl)
  )
}
