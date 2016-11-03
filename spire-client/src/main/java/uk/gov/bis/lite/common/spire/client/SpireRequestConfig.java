package uk.gov.bis.lite.common.spire.client;

public class SpireRequestConfig {

  private final String namespace;
  private final String requestChildName;
  private final boolean useSpirePrefix;

  /**
   * SpireRequest setup configuration data
   *
   * @param namespace        spire namespace
   * @param requestChildName request soap body immediate child element name
   * @param useSpirePrefix   prefix requestChildName with 'spir'
   */
  public SpireRequestConfig(String namespace, String requestChildName, boolean useSpirePrefix) {
    this.namespace = namespace;
    this.requestChildName = requestChildName;
    this.useSpirePrefix = useSpirePrefix;
  }

  String getNamespace() {
    return namespace;
  }

  String getRequestChildName() {
    return requestChildName;
  }

  boolean isUseSpirePrefix() {
    return useSpirePrefix;
  }
}
