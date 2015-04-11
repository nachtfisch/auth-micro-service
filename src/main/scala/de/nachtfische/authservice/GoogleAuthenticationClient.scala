package de.nachtfische.authservice

import akka.actor.ActorSystem
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import spray.client.pipelining._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

import scala.collection.JavaConversions
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Try, Success}


class GoogleAuthenticationClient(system:ActorSystem, googleProviderInfo: GoogleProviderInfo) {

  case class AccessTokenResponse(access_token:String, id_token:String, expires_in:Int, token_type:String, refresh_token:Option[String])

  implicit val accessTokenResponseFormat = jsonFormat5(AccessTokenResponse)

  implicit val systemImpl = ActorSystem() // only here to make it implicitly available

  import system.dispatcher

  def getGoogleAuthorizationCodeRequestUrl():String = {
    new AuthorizationCodeRequestUrl("https://accounts.google.com/o/oauth2/auth",
      googleProviderInfo.clientId)
      .setRedirectUri(googleProviderInfo.redirectUri)
      .setScopes(JavaConversions.asJavaCollection(Seq("email", "openid", "profile")))
      .setState("someSecretState")
      .toString
  }

  def getAccessToken(code: String): Try[String] = {
    val pipeline: HttpRequest => Future[AccessTokenResponse] = (
        sendReceive
        ~> unmarshal[AccessTokenResponse]
      )
    val tokenResult = pipeline(Post("https://www.googleapis.com/oauth2/v3/token", FormData(Seq(
      "code" -> code,
      "client_id" -> googleProviderInfo.clientId,
      "client_secret" -> googleProviderInfo.clientSecret,
      "redirect_uri" -> googleProviderInfo.redirectUri,
      "grant_type" -> "authorization_code"
    ))))

    Await.ready(tokenResult, Duration.Inf).value.get.map(t => t.access_token)
  }

  def getUserInfo(accessCode: String): Try[String] = {
    case class ProfileResponse(email:String)
    implicit val profileResponseFormat = jsonFormat1(ProfileResponse)

    val pipeline: HttpRequest => Future[ProfileResponse] = (
        addHeader(HttpHeaders.Authorization(OAuth2BearerToken(accessCode)))
        ~> sendReceive
        ~> unmarshal[ProfileResponse]
      )

    val pipeline1: Future[ProfileResponse] = pipeline(
      Get("https://www.googleapis.com/oauth2/v1/userinfo")
    )
    Await.ready(pipeline1, Duration.Inf).value.get.map(t => t.email)

  }

}
