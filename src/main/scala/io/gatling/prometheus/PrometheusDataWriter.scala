/*
 * Copyright 2011-2019 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.prometheus

import io.gatling.commons.util.Clock
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.stats.message.{End, Start}
import io.gatling.core.stats.writer._
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.{Counter, Histogram}

import java.io.IOException
import java.util.Objects

case class PrometheusData(
    startedUsers:       Counter,
    finishedUsers:      Counter,
    requestLatencyHist: Histogram,
    errorCounter:       Counter,
    simulation:         String,
    server:             Option[HTTPServer]
) extends DataWriterData

class PrometheusDataWriter(clock: Clock, configuration: GatlingConfiguration) extends DataWriter[PrometheusData] {

  override def onInit(init: Init): PrometheusData = {
    var port = configuration.data.prometheus.port
    var server: Option[HTTPServer] = None
    try {
      logger.info(s"trying to start Prometheus Endpoint Server on port: $port")
      server = Option(new HTTPServer(port, true))
      port = server.get.getPort
      logger.info(s"Started Prometheus Endpoint Server on port: $port")

    } catch {
      case e: IOException => {
        logger.info(String.format(s"Failed to start the Prometheus server on port $port, Got IO Exception"))
        e.printStackTrace()
      }
    }
    PrometheusData(
      startedUsers = Counter.build
        .name("total_started_users").labelNames("simulation", "scenario")
        .help("Total Gatling users Started").register,
      finishedUsers = Counter.build
        .name("total_finished_users").labelNames("simulation", "scenario")
        .help("Total Gatling users Finished").register,
      requestLatencyHist = Histogram.build
        .name("requests_latency_secondsHistogram")
        .help("Request latency in ms.").labelNames("simulation", "metric", "group", "sim_group_metric_hash", "error", "responseCode", "oK")
        .buckets(.005, .01, .025, .05, .075, .1, .25, .5, .75, .8, 1, 1.2, 2.5, 5, 7.5, 10).register,
      errorCounter = Counter.build
        .name("error_msg_count")
        .help("Keeps count of each error message").labelNames("simulation", "errorMsg").register,
      simulation = init.runMessage.simulationId,
      server = server
    )
  }

  override def onMessage(message: LoadEventMessage, data: PrometheusData): Unit = message match {
    case user: UserMessage         => onUserMessage(user, data)
    case response: ResponseMessage => onResponseMessage(response, data)
    case error: ErrorMessage       => onErrorMessage(error, data)
    case _                         =>
  }

  override def onFlush(data: PrometheusData): Unit = {}

  override def onCrash(cause: String, data: PrometheusData): Unit = {
    if (data.server.isDefined)
      data.server.get.stop()
  }

  def onStop(data: PrometheusData): Unit = {
    if (data.server.isDefined)
      data.server.get.stop()
  }

  private def onUserMessage(user: UserMessage, data: PrometheusData): Unit = {
    import user._

    event match {
      case Start =>
        data.startedUsers.labels(data.simulation, user.session.scenario).inc()
      case End =>
        data.finishedUsers.labels(data.simulation, user.session.scenario).inc()
    }
  }

  private def onResponseMessage(response: ResponseMessage, data: PrometheusData): Unit = {
    import response._

    logger.debug(s"Received Response message, ${name}")

    val group = response.groupHierarchy.mkString("_");
    val simGroupMetric = Objects.hash(data.simulation, group, name).toString

    data.requestLatencyHist.labels(
      data.simulation, name, group, simGroupMetric, message.getOrElse(""), responseCode.getOrElse("0"), status.toString
    )
      .observe((endTimestamp - startTimestamp) / 1000D)
  }

  private def onErrorMessage(error: ErrorMessage, data: PrometheusData): Unit = {
    data.errorCounter.labels(data.simulation, error.message).inc()
  }

}
