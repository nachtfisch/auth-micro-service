package com.example

import org.specs2.mutable.Specification
import spray.http
import spray.http.HttpHeaders.Authorization
import spray.json.{JsObject, JsValue}
import spray.routing.MalformedHeaderRejection
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class RESTServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system

  import spray.httpx.SprayJsonSupport._
  import ServiceJsonFormat._

  "REST service" should {

    "return access token with valid credentials and access secure url" in {
      val response = Post("/login", Credentials("some@some.de", "password")) ~> myRoute ~> check {
        responseAs[String] must contain("token")
        responseAs[JsObject]
      }
      val accessToken: Option[JsValue] = response.fields.get("token")
      accessToken must not be None

      Get("/secure2").withHeaders(Authorization(OAuth2BearerToken(accessToken.get.toString()))) ~> myRoute ~> check {
        status must equalTo(OK)
      }
    }

    "reject request to secure url if no bearer token is used" in {
      Get("/secure2").withHeaders(Authorization(BasicHttpCredentials("a"))) ~> myRoute ~> check {
        status must not equalTo(OK)
      }
    }

    "reject request to secure url if no valid token is used" in {
      Get("/secure2").withHeaders(Authorization(OAuth2BearerToken("invalidToken"))) ~> myRoute ~> check {
        status must not equalTo(OK)
      }
    }

    "reject request to secure url if no valid token is used" in {
      Get("/secure2").withHeaders(Authorization(OAuth2BearerToken("invalidToken"))) ~> myRoute ~> check {
        status must not equalTo(OK)
      }
    }




  }
}
