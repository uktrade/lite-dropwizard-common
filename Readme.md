# LITE Dropwizard Common

Common code for [Dropwizard](http://www.dropwizard.io/) projects created by the LITE team.

## Repository Config

Add the following repositories to your build file to be able to use the libraries from this project:

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

### [JWT](jwt)

Support for JWT authorisation within LITE services.

### [Readiness Metric](readiness-metric)

Support for adding readiness metrics accessible via the admin port.  

### [Spire Client](spire-client)

Support for accessing data from Spire.

## Adding a new library

This project is configured as a [Gradle multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds.html).
To add a new library, create it in a new subdirectory and add the name to `settings.gradle` in the main directory. Most
configuration options will be inherited from the main `build.gradle`, but the library's `build.gradle` must specify its
version and dependencies.

To build or publish a library, the simplest way is to execute Gradle tasks from its directory. E.g. to build just the 
JWT library:

```
cd jwt
../gradlew clean build
```