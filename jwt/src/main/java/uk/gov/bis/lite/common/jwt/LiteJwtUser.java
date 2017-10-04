package uk.gov.bis.lite.common.jwt;

import java.security.Principal;

public class LiteJwtUser implements Principal {
  private final String userId;
  private final String email;

  public LiteJwtUser(String userId, String email) {
    this.userId = userId;
    this.email = email;
  }

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  @Override
  public String getName() {
    return userId;
  }
}
