package uk.gov.bis.lite.common.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.Principal;

public class LiteJwtUser implements Principal {
  private String userId;
  private String email;
  private String fullName;

  public LiteJwtUser() {

  }

  public LiteJwtUser(String userId, String email, String fullName) {
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
  }

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

  @Override
  @JsonIgnore
  public String getName() {
    return userId;
  }
}
