package uk.gov.bis.lite.common.auth.basic;

import io.dropwizard.auth.Authorizer;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleAuthorizer implements Authorizer<User> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAuthorizer.class);

  @Override
  public boolean authorize(User user, String roleString) {
    Role role = EnumUtils.getEnum(Role.class, roleString);
    if (role == null) {
      LOGGER.error("Unknown role " + roleString);
      return false;
    } else if (user == null) {
      LOGGER.error("User is null");
      return false;
    } else if (user.getRoles() == null) {
      LOGGER.error("User roles are null");
      return false;
    } else {
      return user.getRoles().contains(role);
    }
  }

}
