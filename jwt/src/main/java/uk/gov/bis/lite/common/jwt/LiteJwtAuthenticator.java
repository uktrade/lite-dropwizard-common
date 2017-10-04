package uk.gov.bis.lite.common.jwt;

import io.dropwizard.auth.Authenticator;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class LiteJwtAuthenticator implements Authenticator<JwtContext, LiteJwtUser>{

  private static final Logger LOGGER = LoggerFactory.getLogger(LiteJwtAuthenticator.class);

  @Override
  public Optional<LiteJwtUser> authenticate(JwtContext context) {
    // All JsonWebTokenExceptions will result in a 401 Unauthorized response.
    try {
      String userId = context.getJwtClaims().getSubject();
      String email = context.getJwtClaims().getStringClaimValue("email");

      if (!StringUtils.isBlank(userId)) {
        LOGGER.info("JWT: sub \"{}\" email \"{}\"", userId, email);
        return Optional.of(new LiteJwtUser(userId, email));
      } else {
        LOGGER.info("JWT: invalid sub \"{}\"");
      }

      return Optional.empty();
    } catch (MalformedClaimException e) {
      return Optional.empty();
    }
  }
}
