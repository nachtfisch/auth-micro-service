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

    "access secure url with valid token" in {
      Post("/login", Credentials("some@some.de", "password")) ~> myRoute ~> check {
        responseAs[String] must contain("token")
        responseAs[JsObject]
      }
    }

    "access secure url with valid token returns OK" in {
      val validToken = generateValidToken

      Get("/secure2").withHeaders(Authorization(OAuth2BearerToken(validToken))) ~> myRoute ~> check {
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

    "reject request to secure url if no 'Authorization' header is set" in {
      Get("/secure2") ~> myRoute ~> check {
        status must not equalTo(OK)
      }
    }

    def generateValidToken: String = {
      val response = Post("/login", Credentials("some@some.de", "password")) ~> myRoute ~> check {
        responseAs[JsObject]
      }
      response.fields.get("token").get.toString
    }

  }
}
