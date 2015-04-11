package de.nachtfische.authservice

import java.nio.charset.StandardCharsets
import java.util.Base64

import akka.actor.ActorRefFactory
import org.specs2.mutable.Specification
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpService
import spray.testkit.Specs2RouteTest


class UserRestSpec extends Specification with Specs2RouteTest with HttpService with AuthenticationRoutes  {

  val uri = "/v1/accounts"

  def actorRefFactory: ActorRefFactory = system
  override val googleAuthenticationClient: GoogleAuthenticationClient = new GoogleAuthenticationClient(system, GoogleProviderInfo("","",""))


    s"POST %uri" should {

    val expected = CreateAccountRequest("false", "password")

    "return OK and id of account" in {
       Post(uri, expected) ~> route ~> check {
         val accountCreatedResponse = responseAs[CreateAccountResponse]

         response.status must equalTo(OK)
         accountCreatedResponse.href must startWith("/v1/accounts/")
       }
    }

    "create account and get it" in {
      Post(uri, expected) ~> route ~> check {
        val accountCreatedResponse = responseAs[CreateAccountResponse]

        Get(s"${accountCreatedResponse.href}") ~> route ~> check {
          response.status must equalTo(OK)
        }
      }
    }

  }

  s"POST to /v1/application/loginAttempts" should {
    "login for valid credentials" in {
      Post("/v1/application/loginAttempts", LoginAttemptsRequest(encodeBase64Utf8("user2:pass"))) ~> route ~> check {
        val errorResponse: ErrorResponse = responseAs[ErrorResponse]
        errorResponse.message must equalTo("fancy")
        response.status must equalTo(BadRequest)
      }
    }
  }

  def encodeBase64Utf8(toEncode:String):String = {
    Base64.getEncoder.encodeToString(toEncode.getBytes(StandardCharsets.UTF_8))
  }

}




