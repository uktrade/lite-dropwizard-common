package uk.gov.bis.lite.common.jwt;

import com.google.inject.Inject;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

public class LiteJwtUserHelper {
  private final LiteJwtConfig liteJwtConfig;

  @Inject
  public LiteJwtUserHelper(LiteJwtConfig liteJwtConfig) {
    this.liteJwtConfig = liteJwtConfig;
  }

  /**
   * Generates a JWT for the supplied LiteJwtUser
   * @param liteJwtUser the LiteJwtUser, forms the "sub" "email" and "fullName" claims
   * @return a JWT
   */
  public String generateToken(LiteJwtUser liteJwtUser) {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer(liteJwtConfig.getIssuer());
    claims.setExpirationTimeMinutesInTheFuture(10);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(2);
    claims.setSubject(liteJwtUser.getUserId());
    claims.setClaim("email", liteJwtUser.getEmail());
    claims.setClaim("fullName", liteJwtUser.getFullName());

    JsonWebSignature jws = new JsonWebSignature();
    jws.setHeader(HeaderParameterNames.TYPE, "JWT");
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
    jws.setKey(new HmacKey(liteJwtConfig.getKey().getBytes()));
    jws.setPayload(claims.toJson());

    try {
      return jws.getCompactSerialization();
    } catch (JoseException e) {
      throw new RuntimeException("Exception during serialization to token", e);
    }
  }

  /**
   * Generates a JWT for the supplied LiteJwtUser, returns in the format requires for the Authorization header.
   * <pre>{@code Authorization: Bearer <token>}</pre>
   * @see LiteJwtUserHelper#generateToken(LiteJwtUser)
   * @param liteJwtUser the LiteJwtUser, forms the "sub" "email" and "fullName" claims
   * @return a JWT
   */
  public String generateTokenInAuthHeaderFormat(LiteJwtUser liteJwtUser) {
    return formatTokenForAuthHeader(generateToken(liteJwtUser));
  }

  /**
   * Returns a token in the format required for the Authorization header.
   * @param token the JWT to format
   * @return the formatted token
   */
  public String formatTokenForAuthHeader(String token) {
    return "Bearer " + token;
  }

}
