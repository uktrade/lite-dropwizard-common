package uk.gov.bis.lite.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.Map;

import javax.ws.rs.core.Response;

public class LiteJwtUserHelperTest extends BaseTest {

  @Test
  public void generateTokenFromLiteJwtUserTest() throws Exception {
    LiteJwtUser liteJwtUser = new LiteJwtUser("123456", "example@example.com", "Mr Test");
    String jwt = LiteJwtUserHelper.generateTokenFromLiteJwtUser(JWT_SHARED_SECRET, "Some lite application", liteJwtUser);

    Response response = getResponse("/auth", jwt);
    String jsonObjBody = response.readEntity(String.class);
    Map<String, String> map = jsonObjToMap(jsonObjBody);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(map.get("name")).isEqualTo("123456");
    assertThat(map.get("userId")).isEqualTo("123456");
    assertThat(map.get("email")).isEqualTo("example@example.com");
    assertThat(map.get("fullName")).isEqualTo("Mr Test");
  }
}