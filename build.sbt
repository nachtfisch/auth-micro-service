import _root_.spray.revolver.RevolverPlugin.Revolver

name := "auth-micro-service"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"     % sprayV,
    "io.spray"            %%  "spray-routing" % sprayV,
    "io.spray"            %%  "spray-client"     % sprayV,
    "io.spray"            %%  "spray-json"    % "1.3.1",
    "org.mongodb"               %%  "casbah"          % "2.7.2",
    "com.novus"                 %%  "salat"           % "1.9.8",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.google.oauth-client" % "google-oauth-client" % "1.20.0",
    "io.spray"            %%  "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

Revolver.settings
