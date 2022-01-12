import sbt._

name := "akka-streams-word-count"

version := "0.1"

scalaVersion := "2.13.7"

val AkkaVersion     = "2.6.18"
val AkkaHTTPVersion = "10.2.6"

val akka = Seq(
  "org.postgresql"              % "postgresql"                         % "42.3.1",
  "com.lightbend.akka"         %% "akka-stream-alpakka-slick"          % "3.0.4",
  "ch.qos.logback"              % "logback-classic"                    % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging"                      % "3.9.4",
  "com.typesafe.akka"          %% "akka-stream"                        % AkkaVersion,
  "com.typesafe.akka"          %% "akka-http"                          % AkkaHTTPVersion,
  "com.typesafe.akka"          %% "akka-http-spray-json"               % AkkaHTTPVersion,
  "com.lightbend.akka"         %% "akka-stream-alpakka-json-streaming" % "3.0.4",
  "com.typesafe.akka"          %% "akka-stream-testkit"                % AkkaVersion     % Test,
  "com.typesafe.akka"          %% "akka-http-testkit"                  % "10.2.6" % Test,
  "org.scalatest"              %% "scalatest"                          % "3.2.9"         % Test,
  "org.scalatestplus"          %% "mockito-3-4"                        % "3.2.10.0"      % Test
)

libraryDependencies ++= akka

assembly / assemblyMergeStrategy := {
  case PathList("reference.conf")    => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _                             => MergeStrategy.first
}
