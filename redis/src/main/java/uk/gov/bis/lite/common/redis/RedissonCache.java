package uk.gov.bis.lite.common.redis;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RedissonCache {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedissonCache.class);

  private final RedissonClient redissonClient;
  private final String keyPrefix;
  private final String key;

  @Inject
  public RedissonCache(RedissonClient redissonClient, String keyPrefix, String key) {
    if (redissonClient == null) {
      throw new IllegalArgumentException("RedissonClient cannot be null.");
    } else if (StringUtils.isBlank(key)) {
      throw new IllegalArgumentException("KeyPrefix cannot be blank.");
    } else if (StringUtils.isBlank(keyPrefix)) {
      throw new IllegalArgumentException("Key cannot be blank.");
    }
    this.redissonClient = redissonClient;
    this.keyPrefix = keyPrefix;
    this.key = key;
  }

  public <T> T get(Supplier<T> supplier, String method, Ttl ttl, String... arguments) {
    return get(supplier, object -> true, method, ttl, arguments);
  }

  public <T> T get(Supplier<T> supplier, Function<T, Boolean> cache, String method, Ttl ttl, String... arguments) {
    String hashKey = hashKey(method, arguments);
    for (String argument : arguments) {
      if (StringUtils.isBlank(argument)) {
        LOGGER.warn("Unable to use cache for hashKey {} containing blank argument.", hashKey);
        return supplier.get();
      }
    }

    RBucket<T> rBucket = redissonClient.getBucket(hashKey);
    T cachedObject = rBucket.get();
    if (cachedObject != null) {
      LOGGER.info("returned cached object {}", hashKey);
      return cachedObject;
    } else {
      try {
        T object = supplier.get();
        if (object != null && cache.apply(object)) {
          rBucket.set(object, ttl.getTimeToLive(), ttl.getTimeUnit());
        }
        return object;
      } catch (Exception exception) {
        LOGGER.error("Unable to get object {}", hashKey, exception);
        return null;
      }
    }
  }

  public <T> Optional<T> getOptional(Supplier<Optional<T>> supplier, String method, Ttl ttl, String... arguments) {
    String hashKey = hashKey(method, arguments);
    for (String argument : arguments) {
      if (StringUtils.isBlank(argument)) {
        LOGGER.warn("Unable to use cache for hashKey {} containing blank argument.", hashKey);
        return supplier.get();
      }
    }

    RBucket<T> rBucket = redissonClient.getBucket(hashKey);
    T cachedObject = rBucket.get();
    if (cachedObject != null) {
      LOGGER.info("returned cached object {}", hashKey);
      return Optional.of(cachedObject);
    } else {
      try {
        Optional<T> object = supplier.get();
        if (object.isPresent()) {
          rBucket.set(object.get(), ttl.getTimeToLive(), ttl.getTimeUnit());
          return object;
        } else {
          return Optional.empty();
        }
      } catch (Exception exception) {
        LOGGER.error("Unable to get object {}", hashKey, exception);
        return Optional.empty();
      }
    }
  }

  private String hashKey(String method, String... arguments) {
    if (StringUtils.isBlank(method)) {
      throw new IllegalArgumentException("Method name must be specified.");
    } else if (arguments.length == 0) {
      throw new IllegalArgumentException("At least one argument must be specified.");
    } else {
      return keyPrefix + ":" + key + ":" + method + ":" + StringUtils.join(arguments, ":");
    }
  }

}
