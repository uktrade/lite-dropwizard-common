package uk.gov.bis.lite.common.auth.basic;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.auth.basic.app.TestApp;
import uk.gov.bis.lite.common.auth.basic.app.TestAppConfig;
import uk.gov.bis.lite.common.auth.basic.util.AuthUtil;

import javax.ws.rs.core.Response;

public class IntegrationTest {

  private static final String AUTH_URL = "http://localhost:8090/auth";

  @ClassRule
  public static final DropwizardAppRule<TestAppConfig> RULE =
      new DropwizardAppRule<>(TestApp.class, resourceFilePath("test-config.yaml"));

  @Test
  public void getShouldReturnOk() {
    Response response = JerseyClientBuilder.createClient()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.SERVICE_USER)
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void deleteShouldReturnOk() {
    Response response = JerseyClientBuilder.createClient()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.ADMIN_USER)
        .delete();

    assertThat(response.getStatus()).isEqualTo(200);
  }

}
