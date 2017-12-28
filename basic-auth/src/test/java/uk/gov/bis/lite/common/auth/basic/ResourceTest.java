package uk.gov.bis.lite.common.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.auth.basic.app.TestResource;
import uk.gov.bis.lite.common.auth.basic.util.AuthUtil;

import javax.ws.rs.core.Response;

public class ResourceTest {

  private static final String AUTH_URL = "/auth";

  @ClassRule
  public static final ResourceTestRule resources = AuthUtil.authBuilder()
      .addResource(new TestResource())
      .build();

  @Test
  public void getShouldReturnOk() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.SERVICE_USER)
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void deleteShouldReturnOk() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.ADMIN_USER)
        .delete();

    assertThat(response.getStatus()).isEqualTo(200);
  }

}
