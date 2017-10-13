# LITE Dropwizard Common

Common code for [Dropwizard](http://www.dropwizard.io/) projects created by the LITE team.

## Repository Config

Add the following repositories to your build file to be able to use the libraries contained here:

``` gradle
repositories {
  maven {
    name "snapshots"
    url "http://nexus.mgmt.int.licensing.service.trade.gov.uk/nexus/content/repositories/snapshots"
  }
  maven {
    name "releases"
    url "http://nexus.mgmt.int.licensing.service.trade.gov.uk/nexus/content/repositories/releases"
  }
}
```

## Libraries

### [Jersey Correlation ID](jersey-correlation-id)

Support for request tracing with Correlation IDs for [Jersey](https://jersey.java.net/) (the RESTful Web Services
framework that [Dropwizard](http://www.dropwizard.io/) runs on) via a pair of filters.

### [JSON Console Appender](json-console-appender)

Allow logging to the console, via [logback](https://logback.qos.ch/) which [Dropwizard](http://www.dropwizard.io/) uses 
for logging, to log data in a JSON format instead of just plain text.

### [Readiness Metric](readiness-metric)

Support for adding readiness metrics accessible via the admin port.  

### [Spire Client](spire-client)

Support for accessing data from Spire.

### [Jwt](jwt)

Support for jwt authorisation within LITE services.