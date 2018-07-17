# Redis

Helpers for running a service with redis caching.

## Getting started

Add the dependency to your `build.gradle`:

```groovy
compile 'uk.gov.bis.lite:redis:0.1.3'
```

## Usage

In your `Configuration` class, add the following:

```java
public class MyConfiguration extends Configuration {
  ...
  @NotNull
  @Valid
  @JsonProperty("redis")
  private RedisConfiguration redisConfiguration;

  public RedisConfiguration getRedisConfiguration() {
    return redisConfiguration;
  }
  ...
}
```

Add the redis configuration to your `config.yaml`, including the method time to live values `ttl`. The time to live values are of format
`methodName: timeToLive timeUnit`, where `timeToLive` is a long and `timeUnit` is any of the enum values in `java.util.concurrent.TimeUnit` in singular or plural.

```yaml
redis:
  host: localhost
  password: ""
  port: 6379
  timeout: 2000
  database: 0
  ssl: false
  poolMinIdle: 1
  poolMaxTotal: 5
  keyPrefix: local
  key: lite-sample-service
  ttl:
    getSample: "1 minute"
    getSamples: "10 days"
```

Register the `RedisModule`, for example via a separate RedisServiceModule. The `RedisModule` parses the `ttl` values from the configuration and registers those with Guice as annotated `Ttl` objects. The `RedisModule` also creates a singleton `RedissonCache` which contains the actual caching functionality.

```java
public class RedisServiceModule extends AbstractModule implements ConfigurationAwareModule<ApplicationConfiguration> {

  private ApplicationConfiguration applicationConfiguration;

  @Override
  protected void configure() {
    install(new RedisModule(applicationConfiguration.getRedisConfiguration()));

    bind(Service.class).to(RedisServiceImpl.class);
  }

  @Override
  public void setConfiguration(ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
  }

}
```

Given an interface Service and a non-redis implementation ServiceImpl, one option is to use the `RedissonCache` and `Ttl` objects via an intermediate class RedisServiceImpl.

```java
public class RedisServiceImpl implements Service {

  private final ServiceImpl serviceImpl;
  private final RedissonCache redissonCache;
  private final Ttl getSample;
  private final Ttl getSamples;

  @Inject
  public RedisServiceImpl(ServiceImpl serviceImpl,
                          RedissonCache redissonCache,
                          @Named("getSample") Ttl getSample,
                          @Named("getSamples") Ttl getSamples) {
    this.serviceImpl = serviceImpl;
    this.redissonCache = redissonCache;
    this.getSample = getSample;
    this.getSamples = getSamples;
  }

  @Override
  public Optional<SampleView> getSample(String sampleId) {
    return redissonCache.getOptional(() -> serviceImpl.getSample(sampleId),
        "getSample",
        getSample,
        sampleId);
  }

  @Override
  public List<SampleView> getSamples(String userId) {
    return redissonCache.get(() -> serviceImpl.getSamples(userId),
        "getSamples",
        getSamples,
        userId);
  }

}
```
