# JWT

Defines helpers objects and methods for consuming a JWT ([Json Web Token](https://jwt.io/)) to a specification common to 
all lite services. Underlying JWT implementation is [jose4j](https://bitbucket.org/b_c/jose4j/wiki/Home).

## Usage

In your Dropwizard `Application` class, add the following snippets to the `run` method
```java
import io.dropwizard.Application;

public class MyApplication extends Application<Configuration> {
  ...
  
  @Override
  public void run(Configuration configuration, Environment environment) throws Exception {
    ...
    
    String jwtSharedSecret = configuration.getJwtSharedSecret(); // demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement 
    
    JwtAuthFilter<LiteJwtUser> liteJwtUserJwtAuthFilter = LiteJwtAuthFilterHelper.buildAuthFilter(jwtSharedSecret);
    
    // As the sole Principal type on the service
    environment.jersey().register(new AuthDynamicFeature(liteJwtUserJwtAuthFilter));
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class));
    
    // Or with multiple Principal types 
    PolymorphicAuthDynamicFeature authFeature = new PolymorphicAuthDynamicFeature(
        ImmutableMap.of(
            MyOtherPrinciple.class, myOtherPrincipleFilter,
            LiteJwtUser.class, liteJwtUserJwtAuthFilter));
    AbstractBinder authBinder = new PolymorphicAuthValueFactoryProvider.Binder<>(ImmutableSet.of(PrincipalImpl.class, LiteJwtUser.class));
    environment.jersey().register(authFeature);
    environment.jersey().register(authBinder);
    ...
    
  } 
}
```

Then add `@Auth LiteJwtUser user` to the parameters of any resource methods which require JWT authorisation

```java
public class MyResource {
  ...
  
  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/some-path/{someParam}")
  public Optional<UserPrivilegesView> viewUserPrivileges(@PathParam("someParam") String someParam, @Auth LiteJwtUser user) {
    ...
    
  }
}

```

## LITE JWT spec
Consumers of this library expect that any provided JWT will conform to the following spec. Any deviation from this spec 
(excluding optional and user defined claims) will return a `401 Unauthorized` response from endpoints making used of this library.

### JWT header
* `typ` (type) "JWT"
* `alg` (algorithm) "HS256"

```json 
{
  "typ": "JWT",
  "alg": "HS256"
}
``` 

This header states that the payload is a JWT and the signature is derived using the HMAC SHA-256 algorithm.

### JWT payload
* `iss` (issued by) is optional not verified. See [iss](https://tools.ietf.org/html/rfc7519#section-4.1.1).
* `exp` (expiration time) required with an allowed clock skew of 30 seconds. See [exp](https://tools.ietf.org/html/rfc7519#section-4.1.4).
* `jti` (JWT ID) is optional and not verified. See [jti](https://tools.ietf.org/html/rfc7519#section-4.1.7).
* `iat` (issued at time) is optional, if provided will be verified with an allowed clock skew of 30 seconds. See [iat](https://tools.ietf.org/html/rfc7519#section-4.1.6).
* `nbf` (not before time) is optional, if provided will be verified with an allowed clock skew of 30 seconds. See [nbf](https://tools.ietf.org/html/rfc7519#section-4.1.5).
* `sub` (subject) required and maps to the `userId` field of `LiteJwtUser`. See [sub](https://tools.ietf.org/html/rfc7519#section-4.1.2).
* `email` (custom email claim) required and maps to the `email` field of `LiteJwtUser`
* `fullName` (custom fullName claim) required and maps to the `fullName` field of `LiteJwtUser`

### Example header
The HTTP Authorization header should be prefixed with "Bearer" and realm "realm" (if provided).

```text
curl -X GET \ -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJTb21lIGxpdGUgYXBwbGljYXRpb24iLCJleHAiOjE1MzkzNjIwMzMsImp0aSI6InA0MDJRMzFkRXlTeTNiWUxlc2Q5a2ciLCJpYXQiOjE1MDc4MjYwMzMsIm5iZiI6MTUwNzgyNTkxMywic3ViIjoiMTIzNDU2IiwiZW1haWwiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwiZnVsbE5hbWUiOiJNciBUZXN0In0.qlu5a6hAVvUO-XrftkLCk_1xqhYjWtCaotR7narg7EU"
```     
     
Decodes to:
```json 
{
  "typ": "JWT",
  "alg": "HS256"
}.
{
  "iss": "Some lite application",
  "exp": 1539362033,
  "jti": "p402Q31dEySy3bYLesd9kg",
  "iat": 1507826033,
  "nbf": 1507825913,
  "sub": "123456",
  "email": "example@example.com",
  "fullName": "Mr Test"
}
```
With a shared secret of "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement" 
