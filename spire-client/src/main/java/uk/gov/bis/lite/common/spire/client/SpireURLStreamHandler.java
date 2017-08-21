package uk.gov.bis.lite.common.spire.client;

import java.io.IOException;
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
    // Clone the URL, which uses URL.getURLStreamHandler to resolve a URLStreamHandler and (on opening a connection) the
    // correct subclass of URLConnection for the supplied URL's protocol.
    URL urlClone = new URL(url.toString());
    URLConnection urlConnection = urlClone.openConnection();
    urlConnection.setConnectTimeout(connectTimeoutMillis);
    urlConnection.setReadTimeout(readTimeoutMillis);
    return urlConnection;
  }
}
