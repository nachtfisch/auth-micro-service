package de.nachtfische.authservice

import akka.actor.{Actor, ActorRefFactory}

class AuthenticationService extends Actor with AuthenticationRoutes {
    implicit val system = context.system

    override implicit def actorRefFactory: ActorRefFactory = context

    override def receive: Receive = runRoute(route)
}
