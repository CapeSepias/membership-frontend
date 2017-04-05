package monitoring

import java.lang.Long
import java.lang.System.currentTimeMillis
import javax.inject._

import com.gu.googleauth.UserIdentity
import controllers.{Cached, NoCache}
import monitoring.SentryLogging.{UserGoogleId, UserIdentityId}
import org.slf4j.MDC
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router
import services.AuthenticationService

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent._

class ErrorHandler @Inject() (
                               env: Environment,
                               config: Configuration,
                               sourceMapper: OptionalSourceMapper,
                               router: Provider[Router]
                               ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def logServerError(request: RequestHeader, usefulException: UsefulException) {
    try {
      for (identityUser <- AuthenticationService.authenticatedUserFor(request)) { MDC.put(UserIdentityId, identityUser.id) }
      for (googleUser <- UserIdentity.fromRequest(request)) { MDC.put(UserGoogleId, googleUser.email.split('@').head) }

      super.logServerError(request, usefulException)

    } finally MDC.clear()
  }

  override def onClientError(request: RequestHeader, statusCode: Int, message: String = ""): Future[Result] = {
    super.onClientError(request, statusCode, message).map(Cached(_))
  }

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    Future.successful(Cached(NotFound(views.html.error404())))
  }

  override protected def onProdServerError(request: RequestHeader, exception: UsefulException): Future[Result] =
    Future.successful(NoCache(InternalServerError(views.html.error500(exception))))

  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    val reference = Long.toString(currentTimeMillis(), 36).toUpperCase
    logServerError(request, new PlayException("Bad request", s"A bad request was received. URI: ${request.uri}, Reference: $reference"))
    Future.successful(NoCache(BadRequest(views.html.error400(request, s"Bad request received. Reference: $reference"))))
  }
}
