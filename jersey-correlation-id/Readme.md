# Jersey Correlation ID

Support for request tracing with Correlation IDs for [Jersey](https://jersey.java.net/) (the RESTful Web Services
framework that [Dropwizard](http://www.dropwizard.io/) runs on) via a pair of filters.

## How to use

Add the dependency to your project:

```gradle
compile 'uk.gov.bis.lite:jersey-correlation-id:1.0-SNAPSHOT'
```

Then, in your Dropwizard application class, add the container filter to Jersey:

```java
environment.jersey().register(ContainerCorrelationIdFilter.class);
```

And anywhere you create Jersey clients add the client filter:

```java
client.register(ClientCorrelationIdFilter.class);
```

Now you can add `%mdc{corrID}` to any logging patterns you have and you should start seeing Correlation IDs in your logs.

```yaml
  appenders:
    - type: console
      logFormat: "[%mdc{corrID}] %d [%p] %marker %logger %m%n"
```
