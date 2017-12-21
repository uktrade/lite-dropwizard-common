package uk.gov.bis.lite.common.paas.db;

import io.pivotal.labs.cfenv.CloudFoundryEnvironment;
import io.pivotal.labs.cfenv.CloudFoundryEnvironmentException;
import io.pivotal.labs.cfenv.CloudFoundryService;
import io.pivotal.labs.cfenv.Environment;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class VcapServicesParser {

  private final Environment environment;

  public VcapServicesParser() {
    environment = System::getenv;
  }

  public VcapServicesParser(Environment environment) {
    this.environment = environment;
  }

  public String getVcapServiceCredential(String credentialName, String serviceTag) {
    CloudFoundryEnvironment cfEnvironment;
    try {
      cfEnvironment = new CloudFoundryEnvironment(environment);
    } catch (CloudFoundryEnvironmentException e) {
      throw new RuntimeException("Failed to initialise CloudFoundryEnvironment", e);
    }

    Optional<CloudFoundryService> pgService = cfEnvironment.getServiceNames().stream()
        .map(cfEnvironment::getService)
        .filter(e -> e.getTags().contains(serviceTag))
        .findFirst();

    if (pgService.isPresent()) {
      Object credential = pgService.get().getCredentials().get(credentialName);
      if (credential != null && StringUtils.isNoneBlank(credential.toString())) {
        return credential.toString();
      } else {
        throw new IllegalArgumentException(String.format("%s not found in credentials (credentials available: %s)", credentialName,
            pgService.get().getCredentials().keySet()));
      }
    } else {
      throw new IllegalArgumentException(String.format("No service with tag '%s' found in CloudFoundryEnvironment", serviceTag));
    }
  }

}
