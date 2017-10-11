package uk.gov.bis.lite.common.spire.client;

public class SpireClientConfig {

  private final String username;
  private final String password;
  private final String url;

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
}
