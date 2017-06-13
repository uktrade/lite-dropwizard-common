# JSON Console Appender

Allow logging to the console, via [logback](https://logback.qos.ch/) which [Dropwizard](http://www.dropwizard.io/) uses 
for logging, to log data in a JSON format instead of just plain text. This uses the 
[logstash logback encoder library](https://github.com/logstash/logstash-logback-encoder) to do the actual log event -> 
JSON conversion.

This library supports Dropwizard 1.0.0+

## How to use

Add the dependencies to your project for this library and its underlying dependencies:

```gradle
compile 'net.logstash.logback:logstash-logback-encoder:4.10'
compile 'ch.qos.logback.contrib:logback-jackson:0.1.5'
compile 'uk.gov.bis.lite:json-console-appender:1.0'
```

You can then change the config file for your Dropwizard project (src/main/resources/config.yaml) to use the new 
appenders this library provides.

```yaml
server:
  requestLog:
    appenders:
      - type: json-console-access

logging:
  appenders:
    - type: json-console
```

Note that other configuration options for the console appender will not work for the json-console appenders.
