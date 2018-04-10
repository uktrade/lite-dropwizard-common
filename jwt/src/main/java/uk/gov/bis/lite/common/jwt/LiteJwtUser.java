package uk.gov.bis.lite.common.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import uk.gov.bis.lite.user.api.view.UserAccountType;

import java.security.Principal;

public class LiteJwtUser implements Principal {
  private String userId;
  private String email;
  private String fullName;
  private UserAccountType userAccountType;

  public String getUserId() {
    return userId;
  }

  public LiteJwtUser setUserId(String userId) {
    this.userId = userId;
    return this;
  }

  public String getEmail() {
    return email;
  }

  public LiteJwtUser setEmail(String email) {
    this.email = email;
    return this;
  }

  public String getFullName() {
    return fullName;
  }

  public LiteJwtUser setFullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  public UserAccountType getUserAccountType() {
    return userAccountType;
  }

  public LiteJwtUser setUserAccountType(UserAccountType userAccountType) {
    this.userAccountType = userAccountType;
    return this;
  }

  @Override
  @JsonIgnore
  public String getName() {
    return getUserId();
  }
}
