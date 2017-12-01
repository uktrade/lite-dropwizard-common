package uk.gov.bis.lite.common.jwt;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiteJwtUserHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(LiteJwtUserHelper.class);

  LiteJwtUserHelper() {
  }

  /**
   * Generates a JWT for the supplied LiteJwtUser
   * @param jwtSharedSecret the shared secret to use with the HMAC SHA-256 algorithm, minimum of 64 bytes
   * @param issuer the "iss" claim
   * @param liteJwtUser the LiteJwtUser, forms the "sub" "email" and "fullName" claims
   * @return a JWT
   */
  public static String generateTokenFromLiteJwtUser(String jwtSharedSecret, String issuer, LiteJwtUser liteJwtUser) {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer(issuer);
    claims.setExpirationTimeMinutesInTheFuture(10);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(2);
    claims.setSubject(liteJwtUser.getName()); // userId
    claims.setClaim("email", liteJwtUser.getEmail());
    claims.setClaim("fullName", liteJwtUser.getFullName());

    JsonWebSignature jws = new JsonWebSignature();
    jws.setHeader(HeaderParameterNames.TYPE, "JWT");
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
    jws.setKey(new HmacKey(jwtSharedSecret.getBytes()));
    jws.setPayload(claims.toJson());

    try {
      return jws.getCompactSerialization();
    } catch (JoseException e) {
      LOGGER.error("JoseException", e);
      return null;
    }
  }
}
