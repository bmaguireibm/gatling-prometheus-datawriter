# Gatling Prometheus DataWriter

This plugin provides a [Prometheus](https://prometheus.io) Datawriter for [Gatling](https://gatling.io).

It opens a Prometheus endpoint on the running Gatling instance to provide real time results to a Prometheus DB.

## Installation

### Maven / Gradle / sbt

This plugin can be installed by adding the following dependency to the `gatling` dependency group in maven / gradle or sbt.

[Plugin dependency](https://search.maven.org/artifact/com.github.bmaguireibm/prometheusplugin_2.12/0.0.1/jar)

maven example:
```
<dependency>
  <groupId>com.github.bmaguireibm</groupId>
  <artifactId>prometheusplugin_2.12</artifactId>
  <version>0.0.1</version>
</dependency>
```
gradle example, working with [Gatling Gradle Plugin](https://github.com/lkishalmi/gradle-gatling-plugin):

```
dependencies {
    compileOnly group: 'org.scala-lang', name: 'scala-library', version: "2.12.6"
    compileOnly group: 'io.gatling', name: 'gatling-app', version: "3.0.3"
    gatling group: 'com.github.bmaguireibm', name: 'prometheusplugin_2.12', version: '0.0.1'
}
```

### Direct Download

If you are using the direct binary download from [gatling.io here](https://gatling.io/download/), you can add the plugin by down loading the [release zip](https://github.com/bmaguireibm/gatling-prometheus-datawriter/releases/tag/3.0.3_0.0.1-beta) and copying the `plugins` dir into `gatling-charts-highcharts-bundle-3.0.3/` and replacing `gatling.sh` or `gatling.bat` in `gatling-charts-highcharts-bundle-3.0.3/bin/` with the corresponding files in the release zip.

The altered .sh and .bat files simply add the `plugins` dir to the classpath ahead of the original gatling classes.

`GATLING_CLASSPATH="$GATLING_HOME/plugins/*:$GATLING_HOME/lib/*:$GATLING_HOME/user-files:$GATLING_CONF:"`


## Configuration

### Gatling

In the ``gatling.conf`` add "prometheus" to the data writers.
You can additionally specify a different port for the metrics to be available at, the default is 9102.


```yaml
  data {
    writers = [console, file, prometheus]
  }

  prometheus {
    port = "9102"  # Port for Prometheus DB to query, must be available.
  }
```

Once a simulations is running, you can test the endpoint is working by browsing to http://localhost:9102 in your browser.
You should see something like the following.

```

  # TYPE total_finished_users counter
  # HELP error_msg_count Keeps count of each error message
  # TYPE error_msg_count counter
  # HELP total_started_users Total Gatling users Started
  # TYPE total_started_users counter
  # HELP requests_latency_secondsHistogram Request latency in seconds.
  # TYPE requests_latency_secondsHistogram histogram
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.005",} 0.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.01",} 0.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.025",} 0.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.05",} 0.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.075",} 0.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.1",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.25",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.5",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="0.75",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="1.0",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="2.5",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="5.0",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="7.5",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="10.0",} 1.0
  requests_latency_secondsHistogram_bucket{simulation="1",metric="testName",error="",responseCode="200",oK="OK",le="+Inf",} 1.0
  requests_latency_secondsHistogram_count{simulation="1",metric="testName",error="",responseCode="200",oK="OK",} 1.0
  requests_latency_secondsHistogram_sum{simulation="1",metric="testName",error="",responseCode="200",oK="OK",} 0.1
```

### Prometheus

[Prometheus](https://prometheus.io/) is an open source time series database and monitoring system.
It uses a pull model, collecting numerical metrics from registered endpoints at a set interval.


#### Install

Prometheus can be installed as a [Docker container](https://prometheus.io/) or installed from [here](https://prometheus.io/download/).
Additionally it is available on Kubernetes via helm with `helm install stable/prometheus`.

#### Configuration

The Following is an example configuration file for Prometheus, found at `/etc/prometheus/prometheus.yml` on linux installs.

```yaml

  global:
    scrape_interval:     15s
    evaluation_interval: 30s

  scrape_configs:
  - job_name: prometheus

    static_configs:
    # Assumes Gatling is running on localhost.
    - targets: ['localhost:9102']
  metrics_path: /metrics
  scheme: http
```

Alternatively, if Gatling is running within a Kubernetes pod, 
you can add the following to the pod `deployment.yaml` to have it automatically picked up by Prometheus.
See [Prometheus Kubernetes configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/#kubernetes_sd_config) for details on how to configure PRometheus for this.

```yaml

    annotations:
      prometheus.io/scrape: "true"
      prometheus.io/port: "9102"
      prometheus.io/scheme: "http"
      prometheus.io/path: "/metrics"
```

