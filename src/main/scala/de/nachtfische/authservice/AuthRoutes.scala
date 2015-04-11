package de.nachtfische.authservice

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.{UUID, Base64}

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl
import spray.http._
import spray.httpx.RequestBuilding
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService

import scala.Predef
import scala.collection.JavaConversions
import scala.collection.mutable.ListBuffer

case class CreateAccountRequest(email:String, password:String)
case class CreateAccountResponse(href:String)

case class LoginAttemptsRequest(value:String)
case class ErrorResponse(message:String)

case class Credentials(email:String, password:String)
case class Account(id:String, email:String)

case class GoogleProvider(clientId:String, clientSecret:String, redirectUri:String)

trait AuthenticationRoutes extends HttpService with DefaultJsonProtocol {

  implicit val accountFormat = jsonFormat2(Account)
  implicit val createAccountRequestFormat = jsonFormat2(CreateAccountRequest)
  implicit val createAccountResponseFormat = jsonFormat1(CreateAccountResponse)
  implicit val loginAttemptsRequestFormat = jsonFormat1(LoginAttemptsRequest)
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)

  val accountService:AccountService

  private val clientID: String = sys.env.getOrElse("GOOGLE_CLIENT_ID", "clientID")
  private val clientSecret: String = sys.env.getOrElse("GOOGLE_CLIENT_SECRET", "clientSecret")
  val google = GoogleProvider(clientID, clientSecret, "http://localhost:8080/google")

  val route =
    pathPrefix("v1" / "accounts") {
      post {
        entity(as[CreateAccountRequest]) { createAccountRequest =>
          val account = accountService.create(Credentials(createAccountRequest.email, createAccountRequest.password))
          complete(CreateAccountResponse(s"/v1/accounts/${account.id}"))
        }
      } ~ path(Segment) { id =>
        get {
          val find = accountService.get(id)
          find match {
            case Some(account) => complete(account)
            case None => complete(HttpResponse(404))
          }

        }
      }
    } ~ pathPrefix("v1" / "application") {
      path("loginAttempts") {
        post {
          entity(as[LoginAttemptsRequest]) { loginAttemptRequest =>
            val decodedBase64Pair = decodeBase64Utf8(loginAttemptRequest.value)

            if (decodedBase64Pair == "user:pass") {
              complete(HttpResponse(200))
            } else {
              complete(HttpResponse(400, HttpEntity(ContentTypes.`application/json`, ErrorResponse("user name does not equal 'user:pass'").toJson.toString())))
            }
          }
        }
      } ~ pathPrefix("v1" / "application" / "google") {
        post {
          parameter('code) { code =>

            val getAccessToken: HttpRequest = RequestBuilding.Post("https://www.googleapis.com/oauth2/v3/token", FormData(Seq(
              "code" -> code,
              "client_id" -> google.clientId,
              "client_secret" -> google.clientSecret,
              "redirect_uri" -> google.redirectUri,
              "grant_type" -> "authorization_code"
              )))
            complete(HttpResponse(400))
          }
        }
      }
    }

  def getGoogleAuthorizationCodeRequestUrl(googleProvider: GoogleProvider):String = {
    new AuthorizationCodeRequestUrl("https://accounts.google.com/o/oauth2/auth", googleProvider.clientId)
      .setRedirectUri(googleProvider.redirectUri)
      .setScopes(JavaConversions.asJavaCollection(Seq("email", "openid")))
      .setState("someSecretState")
      .toString
  }

  def decodeBase64Utf8(some: String): String = {
    new Predef.String(Base64.getDecoder.decode(some), StandardCharsets.UTF_8)
  }
}

trait AccountService {
  def create(credentials: Credentials): Account
  def authenticate(account: Credentials): Option[Account]
  def get(id:String): Option[Account]
}

trait InMemoryAccountService extends AccountService {

  private val toAccount: (AccountRecord) => Account = a => Account(a.id, a.email)
  private val accounts = ListBuffer[AccountRecord]()

  def create(credentials: Credentials): Account = {
    accounts += AccountRecord(generateId, credentials.email, md5(credentials.password))

    Account(generateId, credentials.email)
  }

  def authenticate(credentials: Credentials): Option[Account] = {
    val hash = md5(credentials.password)
    accounts
      .find(a => a.hashedPassword == hash && a.email == credentials.email)
      .map(toAccount)
  }

  def get(id:String) = {
    accounts.find(a => a.id == id).map(toAccount)
  }

  private def generateId: String = {
    UUID.randomUUID.toString
  }

  private def md5(password: String): String = {
    new Predef.String(MessageDigest.getInstance("MD5").digest(password.getBytes))
  }

  case class AccountRecord(id:String, email:String, hashedPassword:String)

}
