package de.nachtfische.authservice

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.{Base64, UUID}

import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.routing.HttpService

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success, Try}

case class CreateAccountRequest(email:String, password:String)
case class CreateAccountResponse(href:String)

case class LoginAttemptsRequest(value:String)
case class ErrorResponse(message:String)

case class Credentials(email:String, password:String)
case class Account(id:String, email:String)

case class GoogleProviderInfo(clientId:String, clientSecret:String, redirectUri:String)

trait AuthenticationRoutes extends HttpService with DefaultJsonProtocol {

  implicit val accountFormat = jsonFormat2(Account)
  implicit val createAccountRequestFormat = jsonFormat2(CreateAccountRequest)
  implicit val createAccountResponseFormat = jsonFormat1(CreateAccountResponse)
  implicit val loginAttemptsRequestFormat = jsonFormat1(LoginAttemptsRequest)
  implicit val errorResponseFormat = jsonFormat1(ErrorResponse)

  val accountService:AccountService = new InMemoryAccountService


  val googleAuthenticationClient:GoogleAuthenticationClient

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
            complete {
               val account:Try[Either[Account,Account]] = googleAuthenticationClient.getAccessToken(code)
                .flatMap(ac => googleAuthenticationClient.getUserInfo(ac))
                .map(email => accountService.getOrCreateProviderAccount(email)) // get or create account here from google directory
              account match {
                case Success(Left(x)) => HttpResponse(200)
                case Success(Right(x)) => HttpResponse(201)
                case Failure(e) => throw e
              }
            }
          }
        }
      }
    }



  def decodeBase64Utf8(some: String): String = {
    new Predef.String(Base64.getDecoder.decode(some), StandardCharsets.UTF_8)
  }
}

trait AccountService {
  def getOrCreateProviderAccount(email: String): Either[Account,Account]
  def create(credentials: Credentials): Account
  def authenticate(account: Credentials): Option[Account]
  def get(id:String): Option[Account]
}

class InMemoryAccountService extends AccountService {

  private val toAccount: (AccountRecord) => Account = a => Account(a.id, a.email)
  private val googleAccounts = ListBuffer[AccountRecord]()
  private val accounts = ListBuffer[AccountRecord]()

  override def create(credentials: Credentials): Account = {
    val id: String = generateId
    accounts += AccountRecord(id, credentials.email, Some(md5(credentials.password)))

    Account(id, credentials.email)
  }

  override def authenticate(credentials: Credentials): Option[Account] = {
    val hash = md5(credentials.password)
    accounts
      .find(a => a.hashedPassword == hash && a.email == credentials.email)
      .map(toAccount)
  }

  override def get(id:String) = {
    accounts.find(a => a.id == id).map(toAccount) orElse googleAccounts.find(a => a.id == id).map(toAccount)
  }

  private def generateId: String = {
    UUID.randomUUID.toString
  }

  private def md5(password: String): String = {
    new Predef.String(MessageDigest.getInstance("MD5").digest(password.getBytes))
  }

  case class AccountRecord(id:String, email:String, hashedPassword:Option[String])

  override def getOrCreateProviderAccount(email: String): Either[Account,Account] = {
    googleAccounts.find( a => a.email == email).map(toAccount).map(Left(_)).getOrElse {
      val id: String = generateId
      googleAccounts += AccountRecord(id, email, None)
      Right(Account(id, email))
    }
  }
}
