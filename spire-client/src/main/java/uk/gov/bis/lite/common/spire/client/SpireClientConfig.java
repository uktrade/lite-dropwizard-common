package uk.gov.bis.lite.common.spire.client;

public class SpireClientConfig {

  private final String username;
  private final String password;
  private final String url;
  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;

  /**
   * Config for Spire Soap connection
   *
   * @param username spire connection username
   * @param password spire connection password
   * @param url      spire connection base url
   */
  public SpireClientConfig(String username, String password, String url) {
    this.username = username;
    this.password = password;
    this.url = url;
    this.connectTimeoutMillis = 20000;
    this.readTimeoutMillis = 60000;
  }

  /**
   * Config for Spire Soap connection
   *
   * @param username              spire connection username
   * @param password              spire connection password
   * @param url                   spire connection base url
   * @param connectTimeoutMillis  connection timeout in milliseconds
   * @param readTimeoutMillis     read timeout in milliseconds
   */
  public SpireClientConfig(String username, String password, String url, int connectTimeoutMillis, int readTimeoutMillis) {
    this.username = username;
    this.password = password;
    this.url = url;
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.readTimeoutMillis = readTimeoutMillis;
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
