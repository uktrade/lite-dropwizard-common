package uk.gov.bis.lite.common.jwt;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

/**
 * Helper class for building a {@code JwtAuthFilter<LiteJwtUser>}object
 */
public class LiteJwtAuthFilterHelper {

  /**
   * Builds a {@link JwtAuthFilter} object which validates a JWT against the following specification.
   * <ul>
   *   <li>{@code type} (type) "JWT"</li>
   *   <li>{@code alg} (algorithm) "HS256" HMAC SHA256 with the key of {@code jwtSharedSecret}</li>
   *   <li>{@code exp} (expiration time) required with an allowed clock skew of 30 seconds</li>
   *   <li>{@code jti} (JWT ID) required</li>
   *   <li>{@code iat} (issued at time) required with an allowed clock skew of 30 seconds</li>
   *   <li>{@code nbf} (not before time) required with an allowed clock skew of 30 seconds</li>
   *   <li>{@code iss} (issued at time) required with an allowed clock skew of 30 seconds</li>
   *   <li>{@code sub} (subject) required</li>
   * </ul>
   *
   * <p>
   *  With the HTTP {@code Authorization} header prefixed with "Bearer" and {@code realm} "realm" (if provided).
   *  <br>
   *  See the following valid example:
   * </p>
   *
   * <pre>
   * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTb21lIGxpdGUgYXBwbGljYXRpb24iLCJleHAiOjE1Mzkz
   * NjIwMzMsImp0aSI6InA0MDJRMzFkRXlTeTNiWUxlc2Q5a2ciLCJpYXQiOjE1MDc4MjYwMzMsIm5iZiI6MTUwNzgyNTkxMywic3ViIjoiMTIzNDU2Iiw
   * iZW1haWwiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwiZnVsbE5hbWUiOiJNciBUZXN0In0.qlu5a6hAVvUO-XrftkLCk_1xqhYjWtCaotR7narg7EU
   * </pre>
   *
   * Decodes to:
   *
   * <blockquote><pre>
   * {
   *  "typ": "JWT",
   *  "alg": "HS256"
   * }.
   * {
   *  "iss": "Some lite application",
   *  "exp": 1539362033,
   *  "jti": "p402Q31dEySy3bYLesd9kg",
   *  "iat": 1507826033,
   *  "nbf": 1507825913,
   *  "sub": "123456",
   *  "email": "example@example.com",
   *  "fullName": "Mr Test"
   *  }
   * </pre></blockquote>
   *
   * With a shared secret of "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement"
   *
   * @param jwtSharedSecret the shared secret to use with the HMAC SHA-256 algorithm, minimum of 64 bytes
   *
   * @return a new {@link JwtAuthFilter}, configured as described here
   */
  public static JwtAuthFilter<LiteJwtUser> buildAuthFilter(String jwtSharedSecret) {
    JwtConsumer consumer = new JwtConsumerBuilder()
        .setAllowedClockSkewInSeconds(30)
        .setRequireIssuedAt()
        .setRequireExpirationTime()
        .setRequireNotBefore()
        .setRequireJwtId()
        .setRequireSubject()
        .setVerificationKey(new HmacKey(jwtSharedSecret.getBytes()))
        .setJwsAlgorithmConstraints(new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA256))
        .build();

    return new JwtAuthFilter.Builder<LiteJwtUser>()
        .setJwtConsumer(consumer)
        .setRealm("realm")
        .setPrefix("Bearer")
        .setAuthenticator(new LiteJwtAuthenticator())
        .buildAuthFilter();
  }
}
