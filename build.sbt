lazy val akkaHttpVersion = "10.1.12"
lazy val akkaVersion    = "2.6.5"

name := "poke-api"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed"     % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream"          % akkaVersion
libraryDependencies += "ch.qos.logback"    % "logback-classic"       % "1.2.3"