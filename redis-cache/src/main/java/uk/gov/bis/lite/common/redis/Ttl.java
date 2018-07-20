package uk.gov.bis.lite.common.redis;

import java.util.concurrent.TimeUnit;

public class Ttl {

  private final boolean cache;
  private final Long timeToLive;
  private final TimeUnit timeUnit;

  private Ttl(boolean cache, Long timeToLive, TimeUnit timeUnit) {
    this.cache = cache;
    this.timeToLive = timeToLive;
    this.timeUnit = timeUnit;
  }

  public boolean isCache() {
    return cache;
  }

  public long getTimeToLive() {
    return timeToLive;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public static Ttl noCache() {
    return new Ttl(false, null, null);
  }

  public static Ttl cache(long timeToLive, TimeUnit timeUnit) {
    return new Ttl(true, timeToLive, timeUnit);
  }

}
