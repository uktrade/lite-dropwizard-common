package uk.gov.bis.lite.common.auth.basic.util;

import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import uk.gov.bis.lite.common.auth.basic.SimpleAuthenticator;
import uk.gov.bis.lite.common.auth.basic.SimpleAuthorizer;
import uk.gov.bis.lite.common.auth.basic.User;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AuthUtil {

  public static final String HEADER = "Authorization";

  public static final String ADMIN_USER = auth("admin:pass");

  public static final String SERVICE_USER = auth("service:password");

  private static final String ADMIN_USER_WRONG_PASSWORD = auth("admin:wrong-pw");

  private static final String SERVICE_USER_WRONG_PASSWORD = auth("service:wrong-pw");

  private static final String ADMIN_USER_NO_PASSWORD = auth("admin:");

  private static final String SERVICE_USER_NO_PASSWORD = auth("service:");

  private static final String UNKNOWN_USER = auth("user:password");

  private static final String WRONGLY_FORMATTED_USER = "Bearer abcd";

  private static String auth(String credentials) {
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
  }

  public static final List<String> UNKNOWN_USERS = Arrays.asList(ADMIN_USER_WRONG_PASSWORD,
      SERVICE_USER_WRONG_PASSWORD,
      ADMIN_USER_NO_PASSWORD,
      SERVICE_USER_NO_PASSWORD,
      UNKNOWN_USER,
      WRONGLY_FORMATTED_USER);

  public static ResourceTestRule.Builder authBuilder() {
    return ResourceTestRule.builder()
        .addProvider(AuthUtil.createAuthProvider())
        .addProvider(RolesAllowedDynamicFeature.class)
        .addProvider(new AuthValueFactoryProvider.Binder<>(User.class));
  }

  private static AuthDynamicFeature createAuthProvider() {
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator("admin",
        "pass",
        "service",
        "password");
    return new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
        .setAuthenticator(simpleAuthenticator)
        .setAuthorizer(new SimpleAuthorizer())
        .setRealm("SUPER SECRET STUFF")
        .buildAuthFilter());
  }

}
