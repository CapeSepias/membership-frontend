package services

import com.gu.identity.play.AccessCredentials.{Cookies, Token}
import com.gu.identity.play.AuthenticatedIdUser.Provider
import configuration.Config
import model.{AccessCredentials, AuthenticatedIdUser, IdMinimalUser}
import play.api.mvc.RequestHeader

object AuthenticationService extends com.gu.identity.play.AuthenticationService {
  def idWebAppSigninUrl(returnUrl: String) = Config.idWebAppSigninUrl(returnUrl)

  val identityKeys = Config.idKeys

  override lazy val authenticatedIdUserProvider: Provider =
    Cookies.authProvider(identityKeys).withDisplayNameProvider(Token.authProvider(identityKeys, "membership"))

}


class AuthenticationService {

  private def copyAccessCredentials(accessCredentials: com.gu.identity.play.AccessCredentials): AccessCredentials =
    accessCredentials match {
      case com.gu.identity.play.AccessCredentials.Cookies(scGuU, guU) => AccessCredentials.Cookies(scGuU, guU)
      case com.gu.identity.play.AccessCredentials.Token(tokenText) => AccessCredentials.Token(tokenText)
    }

  def authenticateUser(request: RequestHeader): Option[AuthenticatedIdUser] =
    AuthenticationService.authenticatedUserFor(request)
      .map { authenticatedIdUser =>
        AuthenticatedIdUser(
          copyAccessCredentials(authenticatedIdUser.credentials),
          IdMinimalUser(authenticatedIdUser.user.id, authenticatedIdUser.user.displayName)
        )
      }
}
