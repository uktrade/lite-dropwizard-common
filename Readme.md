# LITE Dropwizard Common

Common code for [Dropwizard](http://www.dropwizard.io/) projects created by the LITE team.

## Repository Config

Add the following repositories to your build file to be able to use the libraries from this project:

``` gradle
repositories {
  maven {
    name "snapshots"
    url "https://nexus.ci.uktrade.io/repository/maven-snapshots/"
  }
  maven {
    name "releases"
    url "https://nexus.ci.uktrade.io/repository/maven-releases/"
  }
}
```

## Libraries

### [Basic Auth](basic-auth)

Support for running a service with basic authentication for a service and an admin user.

### [Jersey Correlation ID](jersey-correlation-id)

Support for request tracing with Correlation IDs for [Jersey](https://jersey.java.net/) (the RESTful Web Services
framework that [Dropwizard](http://www.dropwizard.io/) runs on) via a pair of filters.

### [JSON Console Appender](json-console-appender)

Allow logging to the console, via [logback](https://logback.qos.ch/) which [Dropwizard](http://www.dropwizard.io/) uses
for logging, to log data in a JSON format instead of just plain text.

### [JWT](jwt)

Support for JWT authorisation within LITE services.

### [PaaS Utils](paas-utils)

Utilities to help services run within GOV.UK PaaS.

### [Readiness Metric](readiness-metric)

Support for adding readiness metrics accessible via the admin port.

### [Redis Cache](redis-cache)

Support for running a service with redis caching.

### [Spire Client](spire-client)

Support for accessing data from Spire.

## Adding a new library

This project is configured as a [Gradle multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds.html).
To add a new library, create it in a new subdirectory and add the name to `settings.gradle` in the main directory. Most
configuration options will be inherited from the main `build.gradle`, but the library's `build.gradle` must specify its
version and dependencies.

To build or test a library, the simplest way is to execute Gradle tasks from its directory. E.g. to build just the
JWT library:

```
cd jwt
../gradlew clean build
```

## Publishing a new library version

To publish a new library version:

* Update the version number in the library's `build.gradle`
* Run the following publish commands:

```
cd <lib-directory>
../gradlew publishMavenJavaPublicationToReleasesRepository -PnexusUsername=<user> -PnexusPassword=<password>
```