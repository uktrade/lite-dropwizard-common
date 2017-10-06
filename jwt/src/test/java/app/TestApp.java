package app;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.setup.Environment;
import uk.gov.bis.lite.common.jwt.LiteJwtAuthFilterHelper;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

public class TestApp extends Application<TestAppConfig> {

  @Override
  public void run(TestAppConfig configuration, Environment environment) throws Exception {
    String jwtSharedSecret = configuration.getJwtSharedSecret();

    JwtAuthFilter<LiteJwtUser> liteJwtUserJwtAuthFilter = LiteJwtAuthFilterHelper.buildAuthFilter(jwtSharedSecret);

    environment.jersey().register(new AuthDynamicFeature(liteJwtUserJwtAuthFilter));

    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class));

    environment.jersey().register(new TestResource());
  }

  public static void main(String[] args) throws Exception {
    new TestApp().run(args);
  }
}
