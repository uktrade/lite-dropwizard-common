package uk.gov.bis.lite.common.jwt;

import org.apache.commons.lang3.StringUtils;

public class LiteJwtConfig {

  private final String key;
  private final String issuer;

  /**
   * @param key    a shared secret to use with the HMAC SHA-256 algorithm, minimum of 64 bytes. Used for signing and verifying JWTs.
   * @param issuer the "iss" claim. Used when generating a JWT.
   */
  public LiteJwtConfig(String key, String issuer) {
    validate(key, issuer);
    this.key = key;
    this.issuer = issuer;
  }

  private void validate(String key, String issuer) {
    if (key == null || key.getBytes().length < 64) {
      throw new RuntimeException("key must be >= 64 bytes in length");
    }
    if (StringUtils.isBlank(issuer)) {
      throw new RuntimeException("issuer must not be empty, blank, or null");
    }
  }

  public String getKey() {
    return key;
  }

  public String getIssuer() {
    return issuer;
  }
}
