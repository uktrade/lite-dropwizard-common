package uk.gov.bis.lite.common.auth.basic;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

public class SimpleAuthenticator implements Authenticator<BasicCredentials, User> {

  private final String adminLogin;
  private final String adminPassword;
  private final String serviceLogin;
  private final String servicePassword;

  public SimpleAuthenticator(String adminLogin, String adminPassword, String serviceLogin, String servicePassword) {
    if (Stream.of(adminLogin, adminPassword, serviceLogin, servicePassword).anyMatch(StringUtils::isBlank)) {
      throw new IllegalArgumentException("Credentials cannot be blank.");
    }
    this.adminLogin = adminLogin;
    this.adminPassword = adminPassword;
    this.serviceLogin = serviceLogin;
    this.servicePassword = servicePassword;
  }

  @Override
  public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
    String username = credentials.getUsername();
    String password = credentials.getPassword();
    if (adminLogin.equals(username) && adminPassword.equals(password)) {
      return Optional.of(new User(username, Arrays.asList(Role.ADMIN, Role.SERVICE)));
    } else if (serviceLogin.equals(username) && servicePassword.equals(password)) {
      return Optional.of(new User(username, Collections.singletonList(Role.SERVICE)));
    } else {
      return Optional.empty();
    }
  }

}
