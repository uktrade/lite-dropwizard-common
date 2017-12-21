package uk.gov.bis.lite.common.paas.db;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.configuration.EnvironmentVariableLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Environment variable subsitutor which applies special treatment to a variable typically named VCAP_JDBC_URL. For this
 * variable, the VCAP_SERVICES environment variable is parsed to determine the URL for the first service found with a
 * specified tag (typically "postgres" to get a PostgreSQL database).
 */
public class CloudFoundryEnvironmentSubstitutor extends StrSubstitutor {

  public static final String DEFAULT_VCAP_JDBC_ENV_VAR = "VCAP_JDBC_URL";
  public static final String DEFAULT_SERVICE_TAG = "postgres";

  /**
   * Creates a substitutor with default parameters - should be sufficient for most cases.
   */
  public CloudFoundryEnvironmentSubstitutor() {
    this(false, DEFAULT_VCAP_JDBC_ENV_VAR, DEFAULT_SERVICE_TAG);
  }

  public CloudFoundryEnvironmentSubstitutor(boolean strict, String jdbcVariableName, String serviceTag) {
    this(strict, new VcapServicesParser(), jdbcVariableName, serviceTag);
  }

  @VisibleForTesting
  CloudFoundryEnvironmentSubstitutor(boolean strict, VcapServicesParser parser, String jdbcVariableName, String serviceTag) {
    super(new JdbcUriLookup(strict, parser, jdbcVariableName, serviceTag));
    this.setEnableSubstitutionInVariables(false);
  }

  private static final class JdbcUriLookup extends EnvironmentVariableLookup {

    private final VcapServicesParser parser;
    private final String jdbcVariableName;
    private final String serviceTag;

    private JdbcUriLookup(boolean strict, VcapServicesParser parser, String jdbcVariableName, String serviceTag) {
      super(strict);
      this.parser = parser;
      this.jdbcVariableName = jdbcVariableName;
      this.serviceTag = serviceTag;
    }

    @Override
    public String lookup(String key) {
      if(jdbcVariableName.equals(key)) {
        //Intercept lookup of the VCAP_JDBC_URL variable and defer to the VCAP parser
        return parser.getVcapServiceCredential("jdbcuri", serviceTag);
      } else {
        //Resolve all other variables as normal
        return super.lookup(key);
      }
    }
  }
}
