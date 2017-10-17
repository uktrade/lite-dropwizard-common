package uk.gov.bis.lite.common.jwt;

import java.security.Principal;

public class LiteJwtUser implements Principal {
  private final String userId;
  private final String email;
  private final String fullName;

  public LiteJwtUser(String userId, String email, String fullName) {
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
  }

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  @Override
  public String getName() {
    return userId;
  }
}
