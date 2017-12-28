package uk.gov.bis.lite.common.auth.basic;

import java.security.Principal;
import java.util.List;

public class User implements Principal {

  private final String name;
  private final List<Role> roles;

  public User(String name, List<Role> roles) {
    this.name = name;
    this.roles = roles;
  }

  @Override
  public String getName() {
    return name;
  }

  public List<Role> getRoles() {
    return roles;
  }

}
