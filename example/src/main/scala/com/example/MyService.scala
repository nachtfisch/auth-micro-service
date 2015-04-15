package com.example

import akka.actor.Actor
import authentikat.jwt.JsonWebToken
import spray.http.HttpHeaders.Authorization
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import spray.routing._
import spray.http._
import MediaTypes._
import spray.routing.authentication.ContextAuthenticator
import spray.routing.directives.AuthMagnet
import spray.routing.directives.AuthMagnet.fromContextAuthenticator
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

  def authenticateUserFn: ContextAuthenticator[UserProfile] = { ctx => {
          Future {
            // parse authorization here
            Right(UserProfile("some@some.de"))
          }
      }
  }

  def authenticateUser = fromContextAuthenticator(authenticateUserFn)


  val myRoute =
    path("login") {
      import ServiceJsonFormat._

      post {
        entity(as[Credentials]) { credentials =>
          complete {
            if (credentials.email == "some@some.de" && credentials.password == "someOther") {
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
        authenticate(authenticateUser) { user =>
          complete(HttpResponse(200))
        }
      }
    }~ path("resources" / Rest) { path =>

      getFromResource(path)
    }
}
