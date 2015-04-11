package de.nachtfische.authservice

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

object Boot extends App {
  implicit val system = ActorSystem("polyglot")
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val service = system.actorOf(Props[AuthenticationService], "rest-service")

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
