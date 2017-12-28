package uk.gov.bis.lite.common.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.auth.basic.app.TestResource;
import uk.gov.bis.lite.common.auth.basic.util.AuthUtil;

import javax.ws.rs.core.Response;

public class ResourceAuthTest {

  private static final String AUTH_URL = "/auth";

  @ClassRule
  public static final ResourceTestRule resources = AuthUtil.authBuilder()
      .addResource(new TestResource())
      .build();

  // GET /auth

  @Test
  public void getShouldReturnOkForAdminUser() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.ADMIN_USER)
        .get();

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void getShouldReturnUnauthorisedForNoAuthHeader() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .get();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void getShouldReturnUnauthorisedForUnknownUser() {
    for (String user : AuthUtil.UNKNOWN_USERS) {
      Response response = resources.client()
          .target(AUTH_URL)
          .request()
          .header(AuthUtil.HEADER, user)
          .get();

      assertThat(response.getStatus()).isEqualTo(401);
    }
  }

  // DELETE /auth

  @Test
  public void deleteShouldReturnForbiddenForServiceUser() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .header(AuthUtil.HEADER, AuthUtil.SERVICE_USER)
        .delete();

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  public void deleteShouldReturnUnauthorisedForNoAuthHeader() {
    Response response = resources.client()
        .target(AUTH_URL)
        .request()
        .delete();

    assertThat(response.getStatus()).isEqualTo(401);
  }

  @Test
  public void deleteShouldReturnUnauthorisedForUnknownUser() {
    for (String user : AuthUtil.UNKNOWN_USERS) {
      Response response = resources.client()
          .target(AUTH_URL)
          .request()
          .header(AuthUtil.HEADER, user)
          .delete();

      assertThat(response.getStatus()).isEqualTo(401);
    }
  }

}
