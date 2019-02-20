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

import akka.testkit.TestFSMRef
import com.typesafe.scalalogging.LazyLogging
import io.gatling.AkkaSpec
import io.gatling.commons.stats.Status
import io.gatling.commons.util.DefaultClock
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.stats.writer.{ Init, ResponseMessage, RunMessage, ShortScenarioDescription }
import io.gatling.commons.stats.assertion.Assertion

import scala.collection.mutable

class PrometheusDataWriterSpec extends AkkaSpec with LazyLogging {

  @throws(classOf[java.io.IOException])
  @throws(classOf[java.net.SocketTimeoutException])
  def get(
    url:            String,
    connectTimeout: Int    = 5000,
    readTimeout:    Int    = 5000,
    requestMethod:  String = "GET"
  ): String =
    {
      import java.net.{ URL, HttpURLConnection }
      val connection = new URL(url).openConnection.asInstanceOf[HttpURLConnection]
      connection.setConnectTimeout(connectTimeout)
      connection.setReadTimeout(readTimeout)
      connection.setRequestMethod(requestMethod)
      val inputStream = connection.getInputStream
      val content = scala.io.Source.fromInputStream(inputStream).mkString
      if (inputStream != null) inputStream.close()
      content
    }

  "PrometheusDataWriter" should "initialize without error and serve endpoint" in {
    // Picks any open port
    var port = 0
    val configuration: GatlingConfiguration = GatlingConfiguration.loadForTest(
      mutable.Map("gatling.data.prometheus.port" -> port)
    )

    val prometheusDataWriter = TestFSMRef(new PrometheusDataWriter(new DefaultClock, configuration))

    prometheusDataWriter.stateName.getClass.getName shouldBe "io.gatling.core.stats.writer.Uninitialized$"

    prometheusDataWriter ! Init(
      Seq.empty[Assertion],
      RunMessage("testSimulation", "1", 1, "test desc", "test version"),
      Seq.empty[ShortScenarioDescription]
    )
    prometheusDataWriter.stateName.getClass.getName shouldBe "io.gatling.core.stats.writer.Initialized$"

    port = prometheusDataWriter.stateData.asInstanceOf[PrometheusData].server.get.getPort

    prometheusDataWriter ! ResponseMessage(
      "test scenario", 1, List.empty[String], "testName", 100, 200, Status("OK"), Option("200"), None
    )

    // Wait one second for the server to start up and message be processed.
    Thread.sleep(1000L)

    val request = get(s"http://localhost:$port")
    logger.debug(request)

    var reqCount = 0.0
    val requestCountRegex = "requests_latency_secondsHistogram_count\\{[^}]+\\} ([0-9.]+)".r.unanchored
    request match {
      case requestCountRegex(count) => reqCount = count.toFloat
      case _                        => logger.warn("Request count not found")
    }
    reqCount shouldBe 1.0
  }

}
