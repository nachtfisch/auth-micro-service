package de.nachtfische.authservice

import akka.actor.{Actor, ActorRefFactory}

class AuthenticationService extends Actor with AuthenticationRoutes {
  implicit val system = context.system

  override implicit def actorRefFactory: ActorRefFactory = context

  private val info: GoogleProviderInfo = GoogleProviderInfo("clientId", "clientSecret", "http://localhost:8080/google")

  override val googleAuthenticationClient: GoogleAuthenticationClient = new GoogleAuthenticationClient(system, info)

  override def receive: Receive = runRoute(route)
}
