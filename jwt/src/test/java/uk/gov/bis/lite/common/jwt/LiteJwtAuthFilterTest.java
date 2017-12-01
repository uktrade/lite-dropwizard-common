package uk.gov.bis.lite.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.HeaderParameterNames;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.junit.Test;

import java.util.Map;

import javax.ws.rs.core.Response;

public class LiteJwtAuthFilterTest extends BaseTest {

  public static final long EXP_MINUTES_INTO_FUTURE = 10;

  public static final long NBF_MINUTES_INTO_PAST = 2;

  /**
   * Signs given claims with JWT_SHARED_SECRET and HMAC_256
   */
  private static String validSignAndSerialize(JwtClaims claims) throws JoseException {
    JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setKey(new HmacKey(JWT_SHARED_SECRET.getBytes()));
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
    jws.setHeader(HeaderParameterNames.TYPE, "JWT");
    return jws.getCompactSerialization();
  }

  private static NumericDate dateMinutesFromNow(long minutes) {
    NumericDate numericDate = NumericDate.now();
    float secondsOffset = minutes * 60;
    numericDate.addSeconds((long) secondsOffset);
    return numericDate;
  }

  @Test
  public void authNotRequiredTest() throws Exception {
    String target = urlTarget("/noauth");
    Response response = RULE.client().target(target).request().get();
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(body).isEqualTo("noauth");
  }

  @Test
  public void authRequiredTest() throws Exception {
    String target = urlTarget("/auth");
    Response response = RULE.client().target(target).request().get();
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authSuccessTest() throws Exception {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String jsonObjBody = response.readEntity(String.class);
    Map<String, String> map = jsonObjToMap(jsonObjBody);

    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(map.get("name")).isEqualTo("123456");
    assertThat(map.get("userId")).isEqualTo("123456");
    assertThat(map.get("email")).isEqualTo("example@example.com");
    assertThat(map.get("fullName")).isEqualTo("Mr Test");
  }

  @Test
  public void authFailedWrongSignatureAlgTest() throws Exception {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    // Algorithm is "HS512"
    JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setKey(new HmacKey(("demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement-demo-secret-which-is-very-" +
        "long-so-as-to-hit-the-byte-requirement").getBytes()));
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
    jws.setHeader(HeaderParameterNames.TYPE, "JWT");

    String jwt = jws.getCompactSerialization();
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedNoSignatureTest() throws Exception {
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    // Algorithm is "none", no signature attached
    JsonWebSignature jws = new JsonWebSignature();
    jws.setPayload(claims.toJson());
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
    jws.setHeader(HeaderParameterNames.TYPE, "JWT");
    jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);

    String jwt = jws.getCompactSerialization();
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedMissingSubTest() throws Exception {
    // Claim "sub" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedEmptySubTest() throws Exception {
    // Claim "sub" has value ""
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject(""); // Testing this claim
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedBlankSubTest() throws Exception {
    // Claim "sub" has value "     "
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("     "); // Testing this claim
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedNullSubTest() throws Exception {
    // Claim "sub" has value null
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject(null); // Testing this claim
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedMissingEmailTest() throws Exception {
    // Claim "email" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedEmptyEmailTest() throws Exception {
    // Claim "email" has value ""
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", ""); // Testing this claim
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedBlankEmailTest() throws Exception {
    // Claim "email" has value "     "
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "     "); // Testing this claim
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedNullEmailTest() throws Exception {
    // Claim "email" has value null
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", null); // Testing this claim
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedMissingFullNameTest() throws Exception {
    // Claim "fullName" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedEmptyFullNameTest() throws Exception {
    // Claim "fullName" has value ""
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", ""); // Testing this claim

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedBlankFullNameTest() throws Exception {
    // Claim "fullName" has value "     "
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "     "); // Testing this claim

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailedNullFullNameTest() throws Exception {
    // Claim "fullName" has value null
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", null); // Testing this claim

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailTokenExpiredTest() throws Exception {
    // Claim "exp" set to 2 minutes in the past
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTime(dateMinutesFromNow(-2)); // Testing this claim
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailExpMissingTest() throws Exception {
    // Claim "exp" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailJtiMissingTest() throws Exception {
    // Claim "jti" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setIssuedAtToNow();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailIatMissingTest() throws Exception {
    // Claim "iat" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailIatInFutureTest() throws Exception {
    // Claim "iat" set to two minutes into the future
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setIssuedAt(dateMinutesFromNow(2)); // Testing this claim
    claims.setGeneratedJwtId();
    claims.setNotBeforeMinutesInThePast(NBF_MINUTES_INTO_PAST);
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);

    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  public void authFailNbfMissingTest() throws Exception {
    // Claim "nbf" is not included
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);


    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }

  @Test
  public void authFailNbfInFutureTest() throws Exception {
    // Claim "nbf" set to 2 minutes into the future
    JwtClaims claims = new JwtClaims();
    claims.setIssuer("Some lite application");
    claims.setExpirationTimeMinutesInTheFuture(EXP_MINUTES_INTO_FUTURE);
    claims.setGeneratedJwtId();
    claims.setIssuedAtToNow();
    claims.setNotBefore(dateMinutesFromNow(2)); // Testing this claim
    claims.setSubject("123456");
    claims.setClaim("email", "example@example.com");
    claims.setClaim("fullName", "Mr Test");

    String jwt = validSignAndSerialize(claims);
    Response response = getResponse("/auth", jwt);
    String body = response.readEntity(String.class);

    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(body).isEqualTo("Credentials are required to access this resource.");
  }
}
