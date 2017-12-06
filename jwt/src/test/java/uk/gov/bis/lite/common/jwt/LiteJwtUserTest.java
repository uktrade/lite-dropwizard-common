package uk.gov.bis.lite.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Map;

public class LiteJwtUserTest {
  public static final String USER_ID = "123456";
  public static final String EMAIL = "test@example.com";
  public static final String FULL_NAME = "Mr Test Example";

  @Test
  public void jsonTest() throws Exception {
    LiteJwtUser liteJwtUser = new LiteJwtUser()
        .setUserId(USER_ID)
        .setEmail(EMAIL)
        .setFullName(FULL_NAME);

    ObjectMapper mapper = new ObjectMapper();
    String liteJwtUserJson = mapper.writeValueAsString(liteJwtUser);

    Map<String, String> propMap = mapper.readValue(liteJwtUserJson, new TypeReference<Map<String, String>>() {
    });
    assertThat(propMap).hasSize(3);
    assertThat(propMap.get("userId")).isEqualTo(USER_ID);
    assertThat(propMap.get("email")).isEqualTo(EMAIL);
    assertThat(propMap.get("fullName")).isEqualTo(FULL_NAME);

    LiteJwtUser liteJwtUserFromJson = mapper.readValue(liteJwtUserJson, LiteJwtUser.class);
    assertThat(liteJwtUserFromJson.getUserId()).isEqualTo(USER_ID);
    assertThat(liteJwtUserFromJson.getName()).isEqualTo(USER_ID);
    assertThat(liteJwtUserFromJson.getEmail()).isEqualTo(EMAIL);
    assertThat(liteJwtUserFromJson.getFullName()).isEqualTo(FULL_NAME);
  }

}