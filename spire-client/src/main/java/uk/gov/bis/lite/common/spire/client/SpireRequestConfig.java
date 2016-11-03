package uk.gov.bis.lite.common.spire.client;

public class SpireRequestConfig {

  private final String nameSpace;
  private final String requestChildName;
  private final boolean useSpirePrefix;

  public SpireRequestConfig(String nameSpace, String requestChildName, boolean useSpirePrefix) {
    this.nameSpace = nameSpace;
    this.requestChildName = requestChildName;
    this.useSpirePrefix = useSpirePrefix;
  }

  String getNameSpace() {
    return nameSpace;
  }

  String getRequestChildName() {
    return requestChildName;
  }

  boolean isUseSpirePrefix() {
    return useSpirePrefix;
  }
}
