package uk.gov.bis.lite.common.auth.admin;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;

public class AdminConstraintSecurityHandler extends ConstraintSecurityHandler {

  protected static final String ADMIN_ROLE = "admin";

  public AdminConstraintSecurityHandler(String userName, String password) {
    Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE);
    constraint.setAuthenticate(true);
    constraint.setRoles(new String[]{ADMIN_ROLE});
    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");
    setAuthenticator(new BasicAuthenticator());
    addConstraintMapping(cm);
    setLoginService(new AdminLoginService(userName, password));
  }

}