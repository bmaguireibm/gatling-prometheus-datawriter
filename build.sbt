ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.example"

val simpleClient                   = "io.prometheus"                        % "simpleclient"                    % "0.6.0"
val simpleClientHttpServe          = "io.prometheus"                        % "simpleclient_httpserver"         % "0.6.0"
val gatling                        = "io.gatling"                           % "gatling-core"                    % "3.0.3"

val akka                           = "com.typesafe.akka"                   %% "akka-actor"                      % "2.5.20"
val scalaTest                      = "org.scalatest"                       %% "scalatest"                       % "3.0.5"             % "test"
val scalaCheck                     = "org.scalacheck"                      %% "scalacheck"                      % "1.14.0"            % "test"
val akkaTestKit                    = akka.organization                     %% "akka-testkit"                    % akka.revision       % "test"
val mockitoCore                    = "org.mockito"                          % "mockito-core"                    % "2.23.4"            % "test"

val testDeps = Seq(scalaTest, scalaCheck, akkaTestKit, mockitoCore)

lazy val prometheusPlugin = (project in file("."))
  .settings(
    name := "prometheusPlugin",
    libraryDependencies += simpleClient,
    libraryDependencies += simpleClientHttpServe,
    libraryDependencies += gatling,
    libraryDependencies += scalaTest,
    libraryDependencies += scalaCheck,
    libraryDependencies += akkaTestKit,
    libraryDependencies += mockitoCore
  )
