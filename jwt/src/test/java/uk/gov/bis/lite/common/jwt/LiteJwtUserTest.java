package uk.gov.bis.lite.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.bis.lite.user.api.view.AccountType;

import java.util.Map;

public class LiteJwtUserTest {
  private static final String USER_ID = "123456";
  private static final String EMAIL = "test@example.com";
  private static final String FULL_NAME = "Mr Test Example";

  @Test
  public void jsonTest() throws Exception {
    LiteJwtUser liteJwtUser = new LiteJwtUser()
        .setUserId(USER_ID)
        .setEmail(EMAIL)
        .setFullName(FULL_NAME)
        .setAccountType(AccountType.REGULATOR);

    ObjectMapper mapper = new ObjectMapper();
    String liteJwtUserJson = mapper.writeValueAsString(liteJwtUser);

    Map<String, String> propMap = mapper.readValue(liteJwtUserJson, new TypeReference<Map<String, String>>() {
    });
    assertThat(propMap).hasSize(4);
    assertThat(propMap.get("userId")).isEqualTo(USER_ID);
    assertThat(propMap.get("email")).isEqualTo(EMAIL);
    assertThat(propMap.get("fullName")).isEqualTo(FULL_NAME);
    assertThat(propMap.get("accountType")).isEqualTo(AccountType.REGULATOR.getValue());

    LiteJwtUser liteJwtUserFromJson = mapper.readValue(liteJwtUserJson, LiteJwtUser.class);
    assertThat(liteJwtUserFromJson.getUserId()).isEqualTo(USER_ID);
    assertThat(liteJwtUserFromJson.getName()).isEqualTo(USER_ID);
    assertThat(liteJwtUserFromJson.getEmail()).isEqualTo(EMAIL);
    assertThat(liteJwtUserFromJson.getFullName()).isEqualTo(FULL_NAME);
    assertThat(liteJwtUserFromJson.getAccountType()).isEqualTo(AccountType.REGULATOR);
  }

}