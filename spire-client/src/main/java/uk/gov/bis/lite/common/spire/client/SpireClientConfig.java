package uk.gov.bis.lite.common.spire.client;

import java.util.Optional;

import javax.annotation.Nullable;

public class SpireClientConfig {

  public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 20000;
  public static final int DEFAULT_READ_TIMEOUT_MILLIS = 60000;

  private final String username;
  private final String password;
  private final String url;
  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;

  /**
   * Config for Spire Soap connection
   *
   * @param username              spire connection username
   * @param password              spire connection password
   * @param url                   spire connection base url
   *
   * @see #SpireClientConfig(String username, String password, String url, Integer connectTimeoutMillis, Integer readTimeoutMillis)
   */
  public SpireClientConfig(String username, String password, String url) {
    this(username, password, url, null, null);
  }

  /**
   * Config for Spire Soap connection
   *
   * @param username              spire connection username
   * @param password              spire connection password
   * @param url                   spire connection base url
   * @param connectTimeoutMillis  connection timeout in milliseconds, defaults to {@value #DEFAULT_CONNECT_TIMEOUT_MILLIS} when null
   * @param readTimeoutMillis     read timeout in milliseconds, defaults to {@value #DEFAULT_READ_TIMEOUT_MILLIS} when null
   */
  public SpireClientConfig(String username, String password, String url, @Nullable Integer connectTimeoutMillis, @Nullable Integer readTimeoutMillis) {
    this.username = username;
    this.password = password;
    this.url = url;
    this.connectTimeoutMillis = Optional.ofNullable(connectTimeoutMillis).orElse(DEFAULT_CONNECT_TIMEOUT_MILLIS);
    this.readTimeoutMillis = Optional.ofNullable(readTimeoutMillis).orElse(DEFAULT_READ_TIMEOUT_MILLIS);
  }

  String getUsername() {
    return username;
  }

  String getPassword() {
    return password;
  }

  String getUrl() {
    return url;
  }

  public int getConnectTimeoutMillis() {
    return connectTimeoutMillis;
  }

  public int getReadTimeoutMillis() {
    return readTimeoutMillis;
  }
}
