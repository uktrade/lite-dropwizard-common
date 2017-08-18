package uk.gov.bis.lite.common.spire.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public class SpireURLStreamHandler extends URLStreamHandler {

  private final int connectTimeoutMillis;
  private final int readTimeoutMillis;

  public SpireURLStreamHandler(int connectTimeoutMillis, int readTimeoutMillis) {
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.readTimeoutMillis = readTimeoutMillis;
  }

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    URL urlClone = new URL(url.toString());
    HttpURLConnection urlConnectionClone = (HttpURLConnection) urlClone.openConnection();
    urlConnectionClone.setConnectTimeout(connectTimeoutMillis);
    urlConnectionClone.setReadTimeout(readTimeoutMillis);
    return(urlConnectionClone);
  }
}
