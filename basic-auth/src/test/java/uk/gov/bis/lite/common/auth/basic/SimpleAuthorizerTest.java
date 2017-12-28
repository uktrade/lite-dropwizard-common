package uk.gov.bis.lite.common.auth.basic;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class SimpleAuthorizerTest {

  @Test
  public void roleCannotBeNull() {
    User user = new User("service", Collections.singletonList(Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, null);

    assertFalse(authorize);
  }

  @Test
  public void roleCannotBeBlank() {
    User user = new User("service", Collections.singletonList(Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "  ");

    assertFalse(authorize);
  }

  @Test
  public void roleMustBeKnown() {
    User user = new User("service", Collections.singletonList(Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "made-up");

    assertFalse(authorize);
  }

  @Test
  public void userCannotBeNull() {
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(null, "SERVICE");

    assertFalse(authorize);
  }

  @Test
  public void userRolesCannotBeNull() {
    User user = new User("service", null);
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "SERVICE");

    assertFalse(authorize);
  }

  @Test
  public void adminUserShouldBeAuthorizedForAdminRole() {
    User user = new User("admin", Arrays.asList(Role.ADMIN, Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "ADMIN");

    assertTrue(authorize);
  }

  @Test
  public void adminUserShouldBeAuthorizedForServiceRole() {
    User user = new User("admin", Arrays.asList(Role.ADMIN, Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "SERVICE");

    assertTrue(authorize);
  }

  @Test
  public void serviceUserShouldNotBeAuthorizedForAdminRole() {
    User user = new User("service", Arrays.asList(Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "ADMIN");

    assertFalse(authorize);
  }

  @Test
  public void serviceUserShouldBeAuthorizedForServiceRole() {
    User user = new User("service", Arrays.asList(Role.SERVICE));
    SimpleAuthorizer simpleAuthorizer = new SimpleAuthorizer();

    boolean authorize = simpleAuthorizer.authorize(user, "SERVICE");

    assertTrue(authorize);
  }

}
