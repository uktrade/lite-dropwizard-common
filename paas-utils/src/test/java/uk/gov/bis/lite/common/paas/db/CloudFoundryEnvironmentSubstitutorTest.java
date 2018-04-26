package uk.gov.bis.lite.common.paas.db;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CloudFoundryEnvironmentSubstitutorTest {

  private final VcapServicesParser parser = new VcapServicesParser(name -> {
    if ("VCAP_SERVICES".equals(name)) {
      return fixture("validVcapServices.json");
    } else {
      return null;
    }
  });

  @Test
  public void testSubstitutions() {
    CloudFoundryEnvironmentSubstitutor substitutor = new CloudFoundryEnvironmentSubstitutor(false, parser, "VCAP_JDBC_URL", "postgres");
    String substituteString = "The JDBC URL is '${VCAP_JDBC_URL}'";
    String substitutionResult = substitutor.replace(substituteString);

    assertThat(substitutionResult)
        .isEqualTo("The JDBC URL is 'jdbc:postgresql://db.host.com:5432/db_name?user=db_username&password=db_password'");

    substitutor = new CloudFoundryEnvironmentSubstitutor(false, parser, "DIFFERENT_VARIABLE_NAME", "postgres");
    substituteString = "The JDBC URL is '${DIFFERENT_VARIABLE_NAME}'";
    substitutionResult = substitutor.replace(substituteString);

    assertThat(substitutionResult)
        .isEqualTo("The JDBC URL is 'jdbc:postgresql://db.host.com:5432/db_name?user=db_username&password=db_password'");
  }

}