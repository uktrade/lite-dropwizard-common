package uk.gov.bis.lite.common.jwt;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import app.TestApp;
import app.TestAppConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

public abstract class BaseTest {
  public static final String JWT_SHARED_SECRET = "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement";

  @Rule
  public final DropwizardAppRule<TestAppConfig> RULE = new DropwizardAppRule<>(
      TestApp.class, resourceFilePath("test-config.yaml"),
      ConfigOverride.config("jwtSharedSecret", JWT_SHARED_SECRET));

  public String urlTarget(String targetPath) {
    return "http://localhost:" + RULE.getLocalPort() + targetPath;
  }

  public Map<String, String> jsonObjToMap(String jsonObj) throws IOException {
    TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(jsonObj, typeRef);
  }

  /**
   * Sends GET request to given target with given jwt in "Authorization" header
   */
  public Response getResponse(String target, String jwt) {
    return RULE.client().target(urlTarget(target)).request()
        .header("Authorization", "Bearer " + jwt)
        .get();
  }
}
