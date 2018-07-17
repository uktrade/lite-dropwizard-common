package uk.gov.bis.lite.common.redis;

import java.util.concurrent.TimeUnit;

public class Ttl {

  private final long timeToLive;
  private final TimeUnit timeUnit;

  public Ttl(long timeToLive, TimeUnit timeUnit) {
    this.timeToLive = timeToLive;
    this.timeUnit = timeUnit;
  }

  public long getTimeToLive() {
    return timeToLive;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

}
