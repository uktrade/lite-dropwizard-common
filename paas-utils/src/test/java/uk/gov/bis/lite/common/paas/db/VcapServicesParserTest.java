package uk.gov.bis.lite.common.paas.db;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class VcapServicesParserTest {

  private VcapServicesParser parser = new VcapServicesParser(name -> {
    if ("VCAP_SERVICES".equals(name)) {
      return fixture("validVcapServices.json");
    } else {
      return null;
    }
  });

  @Test
  public void testGetVcapServiceCredential() {
    assertThat(parser.getVcapServiceCredential("jdbcuri", "postgres"))
        .isEqualTo("jdbc:postgresql://db.host.com:5432/db_name?user=db_username&password=db_password");

    assertThat(parser.getVcapServiceCredential("username", "postgres")).isEqualTo("db_username");
    assertThat(parser.getVcapServiceCredential("password", "postgres")).isEqualTo("db_password");
  }

  @Test
  public void testGetVcapServiceCredential_InvalidServiceTag() {

    assertThatThrownBy(() -> parser.getVcapServiceCredential("jdbcuri", "invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("No service with tag 'invalid' found");
  }

  @Test
  public void testGetVcapServiceCredential_InvalidCredential() {

    assertThatThrownBy(() -> parser.getVcapServiceCredential("invalid", "postgres"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not found in credentials");
  }
}