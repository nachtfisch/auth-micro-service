package com.example

import akka.actor.Actor
import authentikat.jwt.JsonWebToken
import spray.http.HttpHeaders.Authorization
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json.DefaultJsonProtocol
import spray.routing._
import spray.routing.authentication.ContextAuthenticator
import spray.routing.directives.AuthMagnet._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  case class Credentials(email:String, password:String)
  case class LoginSuccessfulResponse(token:String)
  case class LoginRejected(access_token:String)
  case class UserProfile(email:String)

  object ServiceJsonFormat extends DefaultJsonProtocol {
    implicit val credFormat = jsonFormat2(Credentials)
    implicit val loginSuccessFormat = jsonFormat1(LoginSuccessfulResponse)
    implicit val loginRejectedFormat = jsonFormat1(LoginRejected)
  }

  def jwtAuthenticate(secret: String): ContextAuthenticator[UserProfile] = { ctx => {
          Future {
            ctx.request.header[Authorization] match {
              case Some(Authorization(OAuth2BearerToken(x))) => {
                if (!JsonWebToken.validate(x, "secret")) {
                  Left(MalformedHeaderRejection("Authorization", "invalid signature"))
                } else
                  x match {
                    case JsonWebToken(header, claims, signature) => {
                      Right(UserProfile(claims.asJsonString))
                    }
                    case _ => Left(MalformedHeaderRejection("Authorization", "no valid token"))
                  }
              }
              case Some(Authorization(_)) => Left(MalformedHeaderRejection("Authorization", "only Bearer token type supported"))
              case None => Left(MissingHeaderRejection("Authorization"))
            }
          }
      }
  }

  val myRoute =
    path("login") {
      import ServiceJsonFormat._

      post {

        entity(as[Credentials]) { credentials =>
          complete {
            if (credentials.email == "some@some.de" && credentials.password == "password") {
              import authentikat.jwt._
              val header = JwtHeader("HS256")
              val claimsSet = JwtClaimsSet(Map("email" -> credentials.email))

              val jwt:String = JsonWebToken(header, claimsSet, "secret")

              LoginSuccessfulResponse(jwt)
            } else {
              LoginRejected("not correct username")
            }
          }
        }
      }
    } ~ path("secure") {
      get {
        headerValueByType[Authorization]() { authHeader =>
          complete {
            authHeader match {
              case Authorization(OAuth2BearerToken(x)) => x match {
                case JsonWebToken(header, claims, signature) => HttpResponse(200)
                case _ => HttpResponse(400, "no web token")
              }
              case _ => HttpResponse(400, "no beaerer token")
            }
          }
        }
      }
    } ~ path("secure2") {
      get {
        authenticate(jwtAuthenticate("secret")) { user =>
          complete(HttpResponse(200, s"${user.email}"))
        }
      }
    }~ path("resources" / Rest) { path =>

      getFromResource(path)
    }
}
