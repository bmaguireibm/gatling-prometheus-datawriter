

ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.github.bmaguireibm"

ThisBuild / organizationName := "gatling-prometheus-datawriter"
ThisBuild / organizationHomepage := Some(url("https://github.com/bmaguireibm/gatling-prometheus-datawriter"))

ThisBuild / scmInfo := Some(
    ScmInfo(
        url("https://github.com/bmaguireibm/gatling-prometheus-datawriter"),
        "scm:git@github.com/bmaguireibm/gatling-prometheus-datawriter.git"
    )
)
ThisBuild / developers := List(
    Developer(
        id    = "bmaguireibm",
        name  = "Brian Maguire",
        email = "maguirebrianf@gmail.com",
        url   = url("https://github.com/bmaguireibm")
    )
)

ThisBuild / description := "Prometheus Plugin for Gatling"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/bmaguireibm/gatling-prometheus-datawriter"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishMavenStyle := true

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
    libraryDependencies ++= testDeps
  )

publishTo := sonatypePublishTo.value
