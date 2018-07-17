package uk.gov.bis.lite.common.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;

import java.util.concurrent.TimeUnit;

public class RedisModule extends AbstractModule {

  private final RedisConfiguration redisConfiguration;

  public RedisModule(RedisConfiguration redisConfiguration) {
    this.redisConfiguration = redisConfiguration;
  }

  @Override
  protected void configure() {
    redisConfiguration.getTtl().forEach((key, value) -> {
      String[] values = value.split(" ");
      if (values.length != 2) {
        throw new RuntimeException("Invalid redis configuration.");
      } else {
        long timeToLive = Long.parseLong(values[0]);
        String timeUnitStr = StringUtils.appendIfMissing(values[1], "s");
        TimeUnit timeUnit = TimeUnit.valueOf(timeUnitStr.toUpperCase());
        Ttl ttl = new Ttl(timeToLive, timeUnit);
        bind(Ttl.class).annotatedWith(Names.named(key)).toInstance(ttl);
      }
    });
  }

  @Provides
  @Singleton
  public RedissonCache provideRedissonCache(RedissonClient redissonClient) {
    return new RedissonCache(redissonClient, redisConfiguration.getKeyPrefix(), redisConfiguration.getKey());
  }

  @Provides
  @Singleton
  public RedissonClient provideRedissonClient() {

    boolean useSsl = redisConfiguration.getSsl();
    //add additional "s" to protocol for SSL
    String protocol = useSsl ? "rediss://" : "redis://";

    Config redissonConfig = new Config().setCodec(new JsonJacksonCodec(new ObjectMapper()));

    SingleServerConfig singleServerConfig = redissonConfig.useSingleServer()
        .setAddress(protocol + redisConfiguration.getHost() + ":" + redisConfiguration.getPort())
        .setPassword(StringUtils.defaultIfBlank(redisConfiguration.getPassword(), null))
        .setDatabase(redisConfiguration.getDatabase())
        .setTimeout(redisConfiguration.getTimeout())
        .setDnsMonitoring(false)
        .setDnsMonitoringInterval(-1)
        .setConnectionMinimumIdleSize(redisConfiguration.getPoolMinIdle())
        .setConnectionPoolSize(redisConfiguration.getPoolMaxTotal());

    if (useSsl) {
      //Don't attempt to verify the SSL certificates
      singleServerConfig.setSslEnableEndpointIdentification(false);
    }

    return Redisson.create(redissonConfig);
  }

}
