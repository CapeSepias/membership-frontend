package actions

import actions.Fallbacks._
import com.gu.googleauth.{GoogleGroupChecker, UserIdentity}
import com.gu.membership.salesforce.{PaidMember, Tier}
import com.gu.membership.util.Timing
import com.gu.monitoring.CloudWatch
import com.typesafe.scalalogging.LazyLogging
import configuration.Config
import controllers.IdentityRequest
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc._
import play.twirl.api.Html
import services._

import scala.concurrent.Future

/**
 * These ActionFunctions serve as components that can be composed to build the
 * larger, more-generally useful pipelines in 'CommonActions'.
 *
 * https://www.playframework.com/documentation/2.3.x/ScalaActionsComposition
 */
object Functions extends LazyLogging {

  def resultModifier(f: Result => Result) = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = block(request).map(f)
  }

  def authenticated(onUnauthenticated: RequestHeader => Result = chooseRegister(_)): ActionBuilder[AuthRequest] =
    new AuthenticatedBuilder(AuthenticationService.authenticatedUserFor(_), onUnauthenticated)

  def memberRefiner(onNonMember: RequestHeader => Result = notYetAMemberOn(_)) =
    new ActionRefiner[AuthRequest, AnyMemberTierRequest] {
      override def refine[A](request: AuthRequest[A]) = request.forMemberOpt {
        _.map(member => MemberRequest(member, request)).toRight(onNonMember(request))
      }
    }

  def redirectMemberAttemptingToSignUp(selectedTier: Tier)(req: AnyMemberTierRequest[_]): Result = {
    val selectedTierWouldBeUpgrade = selectedTier > req.member.tier
    if (selectedTierWouldBeUpgrade) tierChangeEnterDetails(selectedTier)(req) else memberHome(req)
  }

  def onlyNonMemberFilter(onMember: AnyMemberTierRequest[_] => Result = memberHome(_)) = new ActionFilter[AuthRequest] {
    override def filter[A](request: AuthRequest[A]) = request.forMemberOpt(_.map(member => onMember(MemberRequest(member, request))))
  }

  def isInAuthorisedGroupGoogleAuthReq(includedGroups: Set[String],
                          errorWhenNotInAcceptedGroups: Html) = new ActionFilter[GoogleAuthRequest] {
    override def filter[A](request: GoogleAuthRequest[A]) =
      isInAuthorisedGroup(includedGroups, errorWhenNotInAcceptedGroups, request.user.email, request)
  }

  def isInAuthorisedGroupIdentityGoogleAuthReq(includedGroups: Set[String],
                          errorWhenNotInAcceptedGroups: Html) = new ActionFilter[IdentityGoogleAuthRequest] {
    override def filter[A](request: IdentityGoogleAuthRequest[A]) =
      isInAuthorisedGroup(includedGroups, errorWhenNotInAcceptedGroups, request.googleUser.email, request)
  }

  def isInAuthorisedGroup(includedGroups: Set[String], errorWhenNotInAcceptedGroups: Html, email: String, request: Request[_]) = {
    val googleDirectoryService = new GoogleGroupChecker(Config.googleDirectoryConfig)
    for (usersGroups <- googleDirectoryService.retrieveGroupsFor(email)) yield {
      if (includedGroups.intersect(usersGroups).nonEmpty) None else {
        logger.info(s"Excluding $email from '${request.path}' - not in accepted groups: $includedGroups")
        Some(unauthorisedStaff(errorWhenNotInAcceptedGroups)(request))
      }
    }
  }

  def paidMemberRefiner(onFreeMember: RequestHeader => Result = changeTier(_)) =
    new ActionRefiner[AnyMemberTierRequest, PaidMemberRequest] {
      override def refine[A](request: AnyMemberTierRequest[A]) = Future.successful {
        request.member match {
          case paidMember: PaidMember => Right(MemberRequest(paidMember, request.request))
          case _ => Left(onFreeMember(request))
        }
      }
    }

  def googleAuthenticationRefiner(onNonAuthentication: RequestHeader => Result = OAuthActions.sendForAuth) = {
    new ActionRefiner[AuthRequest, IdentityGoogleAuthRequest] {
      override def refine[A](request: AuthRequest[A]) = Future.successful {
        //Copy the private helper method in play-googleauth to ensure the user is Google auth'd
        //see https://github.com/guardian/play-googleauth/blob/master/module/src/main/scala/com/gu/googleauth/actions.scala#L59-60
        val userIdentityOpt = googleAuthUserOpt(request).map(IdentityGoogleAuthRequest(_, request))
        userIdentityOpt.toRight(onNonAuthentication(request))
      }
    }
  }

  def googleAuthUserOpt(request: RequestHeader) = UserIdentity.fromRequest(request).filter(_.isValid || !OAuthActions.authConfig.enforceValidity)

  def matchingGuardianEmail(onNonGuEmail: RequestHeader => Result = joinStaffMembership(_).flashing("error" -> "Identity email must match Guardian email")) = new ActionFilter[IdentityGoogleAuthRequest] {
    override def filter[A](request: IdentityGoogleAuthRequest[A]) = {
      for {
        user <- IdentityService(IdentityApi).getFullUserDetails(request.identityUser, IdentityRequest(request))
      } yield {
        if (GuardianDomains.emailsMatch(request.googleUser.email, user.primaryEmailAddress)) None
        else Some(onNonGuEmail(request))
      }
    }
  }

  def metricRecord(cloudWatch: CloudWatch, metricName: String) = new ActionBuilder[Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) =
      Timing.record(cloudWatch, metricName) {
        block(request)
      }
  }
}
