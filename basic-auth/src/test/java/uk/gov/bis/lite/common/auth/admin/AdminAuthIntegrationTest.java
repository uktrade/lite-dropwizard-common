package uk.gov.bis.lite.common.auth.admin;

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

public class AdminAuthIntegrationTest {

  @ClassRule
  public static final DropwizardAppRule<TestAppConfig> RULE =
      new DropwizardAppRule<>(TestApp.class, resourceFilePath("test-config.yaml"));
  private static final String ADMIN_PING_URL = "http://localhost:8090/admin/ping";

  @Test
  public void adminPingReturnOk() {
    Response response = JerseyClientBuilder.createClient()
        .target(ADMIN_PING_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.ADMIN_USER)
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void adminPingReturnUnauthorisedForNoAuthHeader() {
    Response response = JerseyClientBuilder.createClient()
        .target(ADMIN_PING_URL)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

}
