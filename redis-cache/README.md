# Redis Cache

Helpers for running a service with Redis caching.

## Getting started

Add the dependency to your `build.gradle`:

```groovy
compile 'uk.gov.bis.lite:redis-cache:0.1.4'
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

Add the redis configuration to your `config.yaml`, including the method time to live values `ttl`.
The time to live values are of format `methodName: timeToLive timeUnit`, where `timeToLive` is a long and `timeUnit` is any of the enum values in `java.util.concurrent.TimeUnit` in singular or plural.
If a method shouldn't be cached, the constant `no-cache` can be specified.

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
    getUsers: "no-cache"
```

Register the `RedisCacheModule`, for example via a separate RedisServiceModule. The `RedisCacheModule` parses the `ttl` values from the configuration and registers those with Guice as annotated `Ttl` objects.
For example, the `Ttl` object for the method `getSample` would be retrieved via `@Named("getSampleTtl")`.
The `RedisCacheModule` also creates a singleton `RedissonCache` which contains the actual caching functionality.

```java
public class RedisServiceModule extends AbstractModule implements ConfigurationAwareModule<ApplicationConfiguration> {

  private ApplicationConfiguration applicationConfiguration;

  @Override
  protected void configure() {
    install(new RedisCacheModule(applicationConfiguration.getRedisConfiguration()));

    bind(Service.class).to(RedisServiceImpl.class);
  }

  @Override
  public void setConfiguration(ApplicationConfiguration applicationConfiguration) {
    this.applicationConfiguration = applicationConfiguration;
  }

}
```

Given an interface Service and a non-redis implementation ServiceImpl, one option is to use the `RedissonCache` and `Ttl` objects via an intermediate class RedisServiceImpl.
The `RedissonCache` methods `get` and `getOptional` take `String... arguments` as their last arguments.
These are used to construct the key for the cached object, and should therefore match the arguments of the respective interface method.

```java
public class RedisServiceImpl implements Service {

  private final ServiceImpl serviceImpl;
  private final RedissonCache redissonCache;
  private final Ttl getSampleTtl;
  private final Ttl getSamplesTtl;

  @Inject
  public RedisServiceImpl(ServiceImpl serviceImpl,
                          RedissonCache redissonCache,
                          @Named("getSampleTtl") Ttl getSampleTtl,
                          @Named("getSamplesTtl") Ttl getSamplesTtl) {
    this.serviceImpl = serviceImpl;
    this.redissonCache = redissonCache;
    this.getSampleTtl = getSampleTtl;
    this.getSamplesTtl = getSamplesTtl;
  }

  @Override
  public Optional<SampleView> getSample(String sampleId) {
    return redissonCache.getOptional(() -> serviceImpl.getSample(sampleId),
        "getSample",
        getSampleTtl,
        sampleId);
  }

  @Override
  public List<SampleView> getSamples(String userId, String sampleType) {
    return redissonCache.get(() -> serviceImpl.getSamples(userId, sampleType),
        "getSamples",
        getSamplesTtl,
        userId, sampleType);
  }

}
```
