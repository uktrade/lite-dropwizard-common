package uk.gov.bis.lite.common.auth.basic.app;


import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import uk.gov.bis.lite.common.auth.basic.AdminConstraintSecurityHandler;
import uk.gov.bis.lite.common.auth.basic.SimpleAuthenticator;
import uk.gov.bis.lite.common.auth.basic.SimpleAuthorizer;
import uk.gov.bis.lite.common.auth.basic.User;

public class TestApp extends Application<TestAppConfig> {

  @Override
  public void run(TestAppConfig configuration, Environment environment) throws Exception {
    //Authorization and authentication handlers
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator(configuration.getAdminLogin(),
        configuration.getAdminPassword(),
        configuration.getServiceLogin(),
        configuration.getServicePassword());
    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
        .setAuthenticator(simpleAuthenticator)
        .setAuthorizer(new SimpleAuthorizer())
        .setRealm("Test Service Authentication")
        .buildAuthFilter()));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    environment.jersey().register(new TestResource());
    environment.admin().setSecurityHandler(new AdminConstraintSecurityHandler(configuration.getAdminLogin(), configuration.getAdminPassword()));
  }

  public static void main(String[] args) throws Exception {
    new TestApp().run(args);
  }
}
