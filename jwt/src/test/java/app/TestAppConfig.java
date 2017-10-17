package app;

import io.dropwizard.Configuration;

public class TestAppConfig extends Configuration {
  private String jwtSharedSecret;

  public String getJwtSharedSecret() {
    return jwtSharedSecret;
  }
}
