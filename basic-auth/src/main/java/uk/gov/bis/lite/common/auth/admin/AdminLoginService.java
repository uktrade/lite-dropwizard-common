package uk.gov.bis.lite.common.auth.admin;

import static uk.gov.bis.lite.common.auth.admin.AdminConstraintSecurityHandler.ADMIN_ROLE;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.util.security.Password;

import java.util.Objects;

public class AdminLoginService extends AbstractLoginService {

  private final AbstractLoginService.UserPrincipal adminPrincipal;
  private final String adminUserName;

  public AdminLoginService(String userName, String password) {
    this.adminUserName = Objects.requireNonNull(userName);
    this.adminPrincipal = new UserPrincipal(userName, new Password(Objects.requireNonNull(password)));
  }

  @Override
  protected String[] loadRoleInfo(UserPrincipal principal) {
    if (adminUserName.equals(principal.getName())) {
      return new String[]{ADMIN_ROLE};
    }
    return new String[0];
  }

  @Override
  protected UserPrincipal loadUserInfo(String userName) {
    return adminUserName.equals(userName) ? adminPrincipal : null;
  }

}
