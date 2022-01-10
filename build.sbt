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
  "com.typesafe.akka"          %% "akka-persistence"                   % AkkaVersion,
  "com.typesafe.akka"          %% "akka-persistence-testkit"           % AkkaVersion % Test,
  "com.typesafe.akka"          %% "akka-stream-testkit"                % AkkaVersion % Test
)

libraryDependencies ++= akka
