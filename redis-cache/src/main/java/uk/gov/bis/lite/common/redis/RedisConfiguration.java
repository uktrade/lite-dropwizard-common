package uk.gov.bis.lite.common.redis;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class RedisConfiguration {

  @NotNull
  @JsonProperty
  private Boolean ssl;

  @NotEmpty
  @JsonProperty
  private String host;

  @NotNull
  @JsonProperty
  private Integer port;

  @JsonProperty
  private String password;

  @NotNull
  @JsonProperty
  private Integer database;

  @NotNull
  @JsonProperty
  private Integer timeout;

  @NotNull
  @JsonProperty
  private Integer poolMinIdle;

  @NotNull
  @JsonProperty
  private Integer poolMaxTotal;

  @NotEmpty
  @JsonProperty
  private String keyPrefix;

  @NotEmpty
  @JsonProperty
  private String key;

  @NotEmpty
  @JsonProperty
  private Map<String, String> ttl;

  public Boolean getSsl() {
    return ssl;
  }

  public String getHost() {
    return host;
  }

  public Integer getPort() {
    return port;
  }

  public String getPassword() {
    return password;
  }

  public Integer getDatabase() {
    return database;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public Integer getPoolMinIdle() {
    return poolMinIdle;
  }

  public Integer getPoolMaxTotal() {
    return poolMaxTotal;
  }

  public String getKeyPrefix() {
    return keyPrefix;
  }

  public String getKey() {
    return key;
  }

  public Map<String, String> getTtl() {
    return ttl;
  }

}
