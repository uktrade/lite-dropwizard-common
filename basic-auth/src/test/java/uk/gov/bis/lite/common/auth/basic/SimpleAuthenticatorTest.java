package uk.gov.bis.lite.common.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.basic.BasicCredentials;
import org.junit.Test;

import java.util.Optional;

public class SimpleAuthenticatorTest {

  @Test
  public void adminUserShouldBeAuthenticated() throws AuthenticationException {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "1234");

    Optional<User> user = simpleAuthenticator.authenticate(new BasicCredentials("admin", "pass"));

    assertThat(user).isPresent();
    assertThat(user.get().getRoles()).containsExactly(Role.ADMIN, Role.SERVICE);
    assertThat(user.get().getName()).isEqualTo("admin");
  }

  @Test
  public void adminUserPasswordMustMatch() throws AuthenticationException {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "1234");

    Optional<User> user = simpleAuthenticator.authenticate(new BasicCredentials("admin", "made-up"));

    assertThat(user).isEmpty();
  }

  @Test
  public void serviceUserShouldBeAuthenticated() throws AuthenticationException {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "1234");

    Optional<User> user = simpleAuthenticator.authenticate(new BasicCredentials("service", "1234"));

    assertThat(user).isPresent();
    assertThat(user.get().getRoles()).containsExactly(Role.SERVICE);
    assertThat(user.get().getName()).isEqualTo("service");
  }

  @Test
  public void serviceUserPasswordMustMatch() throws AuthenticationException {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "1234");

    Optional<User> user = simpleAuthenticator.authenticate(new BasicCredentials("service", "made-up"));

    assertThat(user).isEmpty();
  }

  @Test
  public void usernameMustMatch() throws AuthenticationException {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "1234");

    Optional<User> user = simpleAuthenticator.authenticate(new BasicCredentials("user", "pass"));

    assertThat(user).isEmpty();
  }

  @Test
  public void adminLoginCannotBeBlank() {
    assertThatThrownBy(() -> new SimpleAuthenticator("  ",
        "pass",
        "service",
        "1234"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Credentials cannot be blank.");
  }

  @Test
  public void adminPasswordCannotBeBlank() {
    assertThatThrownBy(() -> new SimpleAuthenticator("admin",
        "  ",
        "service",
        "1234"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Credentials cannot be blank.");
  }

  @Test
  public void serviceLoginCannotBeBlank() {
    assertThatThrownBy(() -> new SimpleAuthenticator("admin",
        "pass",
        "  ",
        "1234"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Credentials cannot be blank.");
  }

  @Test
  public void servicePasswordCannotBeBlank() {
    assertThatThrownBy(() -> new SimpleAuthenticator("admin",
        "pass",
        "service",
        "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Credentials cannot be blank.");
  }

}
