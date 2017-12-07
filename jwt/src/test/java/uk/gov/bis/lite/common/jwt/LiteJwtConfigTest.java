package uk.gov.bis.lite.common.jwt;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

public class LiteJwtConfigTest {
  public static final String KEY = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";
  public static final String ISSUER = "some-lite-service";

  @Test
  public void normalUseCaseTest() throws Exception {
    LiteJwtConfig liteJwtConfig = new LiteJwtConfig(KEY, ISSUER);
    assertThat(liteJwtConfig.getKey()).isEqualTo(KEY);
    assertThat(liteJwtConfig.getIssuer()).isEqualTo(ISSUER);
  }

  @Test
  public void shortKeyTest() throws Exception {
    assertThatThrownBy(() -> new LiteJwtConfig("short-key", ISSUER))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("key must be >= 64 bytes in length");
  }

  @Test
  public void missingIssuerTest() throws Exception {
    assertThatThrownBy(() -> new LiteJwtConfig(KEY, ""))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("issuer must not be empty, blank, or null");

    assertThatThrownBy(() -> new LiteJwtConfig(KEY, " "))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("issuer must not be empty, blank, or null");

    assertThatThrownBy(() -> new LiteJwtConfig(KEY, null))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("issuer must not be empty, blank, or null");
  }
}