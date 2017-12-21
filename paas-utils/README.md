# PaaS Utils

Helpers for running a service within GOV.UK PaaS.

## Getting started

Add the dependency to your `build.gradle`:

```groovy
compile 'uk.gov.bis.lite:paas-utils:1.0'
```

You may need to add `jcenter()` to your resolvers:

```groovy
repositories {
  mavenCentral()
  jcenter()
  ...
}
```

## PaaS database utilities

### Getting the JDBC URL from VCAP_SERVICES

You can put a "special" variable in your `config.yaml` which will be set to a JDBC URL retrieved from the CloudFoundry 
`VCAP_SERVICES` environment variable. The default configuration assumes you are looking for the URL of a Postgres database
and will return the first one found within `VCAP_SERVICES`.

Update your `config.yaml` so the database URL is set from a variable called ` ${VCAP_JDBC_URL}`:

```yaml
database:
  driverClass: org.postgresql.Driver
  url: ${VCAP_JDBC_URL}
    ...
``` 

Then in your application's `initialize()` method, tell Dropwizard to use a `CloudFoundryEnvironmentSubstitutor`. This
is an overloaded version of an `EnvironmentVariableSubstitutor` which will resolve the `${VCAP_JDBC_URL}` variable from
`VCAP_SERVICES`. 

```java
public void initialize(Bootstrap<AppConfig> bootstrap) {
  bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
      new ResourceConfigurationSourceProvider(), new CloudFoundryEnvironmentSubstitutor()));
  ...
}
```

### Setting the schema

Applications should use their own schema instead of the default "public" schema. To do this, update your application's
`Configuration` class to use the `SchemaAwareDatasourceFactory`:

```java
public class MyConfiguration extends Configuration { 
  ...
  
  @Valid
  @NotNull
  @JsonProperty("database")
  private SchemaAwareDataSourceFactory dataSourceFactory;
    
  ...
}
```
 
You can now set a `schema` value in your `config.yaml`:

```yaml
database:
  driverClass: org.postgresql.Driver
  url: ${VCAP_JDBC_URL}
  schema: myservice
    ...
```

This works by ensuring the `currentSchema` parameter is always set on the JDBC URL before Dropwizard attempts to use it.