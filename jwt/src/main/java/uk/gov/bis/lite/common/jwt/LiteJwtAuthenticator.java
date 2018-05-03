package uk.gov.bis.lite.common.jwt;

import io.dropwizard.auth.Authenticator;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.jwt.util.EnumUtil;
import uk.gov.bis.lite.user.api.view.enums.AccountType;

import java.util.Optional;

public class LiteJwtAuthenticator implements Authenticator<JwtContext, LiteJwtUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(LiteJwtAuthenticator.class);

  @Override
  public Optional<LiteJwtUser> authenticate(JwtContext context) {
    // All JsonWebTokenExceptions will result in a 401 Unauthorized response.
    try {
      String userId = context.getJwtClaims().getSubject();
      String email = context.getJwtClaims().getStringClaimValue("email");
      String fullName = context.getJwtClaims().getStringClaimValue("fullName");
      String accountTypeString = context.getJwtClaims().getStringClaimValue("accountType");
      AccountType accountType = EnumUtil.parse(accountTypeString, AccountType.class);
      LOGGER.info("JWT: sub \"{}\" email \"{}\" fullName \"{}\" accountType \"{}\"", userId, email, fullName, accountTypeString);

      boolean userIdIsValid = !StringUtils.isBlank(userId);
      boolean emailIsValid = !StringUtils.isBlank(email);
      boolean fullNameIsValid = !StringUtils.isBlank(fullName);
      boolean accountTypeIsValid = accountType != null;

      if (userIdIsValid && emailIsValid && fullNameIsValid && accountTypeIsValid) {
        LiteJwtUser liteJwtUser = new LiteJwtUser()
            .setUserId(userId)
            .setEmail(email)
            .setFullName(fullName)
            .setAccountType(accountType);
        return Optional.of(liteJwtUser);
      } else {
        if (LOGGER.isWarnEnabled()) {
          StringBuilder messageSb = new StringBuilder("JWT: invalid claim(s) - ");
          if (!userIdIsValid) {
            messageSb.append("sub \"");
            messageSb.append(userId);
            messageSb.append("\" ");
          }
          if (!emailIsValid) {
            messageSb.append("email \"");
            messageSb.append(email);
            messageSb.append("\" ");
          }
          if (!fullNameIsValid) {
            messageSb.append("fullName \"");
            messageSb.append(fullName);
            messageSb.append("\" ");
          }
          if (!accountTypeIsValid) {
            messageSb.append("accountType \"");
            messageSb.append(accountTypeString);
            messageSb.append("\" ");
          }
          LOGGER.warn(messageSb.toString());
        }
        return Optional.empty();
      }
    } catch (MalformedClaimException e) {
      return Optional.empty();
    }
  }
}
