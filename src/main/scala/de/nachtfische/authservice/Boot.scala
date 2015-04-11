package de.nachtfische.polyglot

import java.util.concurrent.TimeUnit

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import akka.pattern.ask


object Boot extends App {
  implicit val system = ActorSystem("polyglot")
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  val service = system.actorOf(Props[RestService], "rest-service")

  IO(Http) ? Http.Bind(service, interface = "localhost", port = 8080)
}
