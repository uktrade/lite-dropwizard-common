# JWT

Defines helpers objects and methods for consuming a JWT ([Json Web Token](https://jwt.io/)) to a specification common to 
all lite services. 

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
    
    environment.jersey().register(new AuthDynamicFeature(liteJwtUserJwtAuthFilter));
    
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(LiteJwtUser.class));
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
* `sub` (subject) required and maps to the `userId` field of `LiteJwtUser`
* `email` (custom email claim) not required, but will map to the `email` field of `LiteJwtUser`
* `aud` (audience) required with value of "lite"
* `exp` (expiration time) required with an allowed clock skew of 30 seconds
* `iat` (issued at time) is optional, if provided will be verified with an allowed clock skew of 30 seconds
* `iss` (issued by) is optional not verified

Any custom claims (such as `email`) are not required verified.

### Example header
The HTTP Authorization header should be prefixed with "Bearer" and realm "realm" (if provided).

```text
curl -X GET \ -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJTb21lIGxpdGUgc2VydmljZSIsImlhdCI6MTUwNzU0MjM3NiwiZXhwIjoxNjAyMjM2Nzc2LCJhdWQiOiJsaXRlIiwic3ViIjoiMTIzNDU2IiwiZW1haWwiOiJleGFtcGxlQGV4YW1wbGUuY29tIn0.wC_Jc4cOoM4UFX7UHHD3hCUcz8b9UPL_ImncY5FtAho"
```     
     
Decodes to:
```json 
{
  "typ": "JWT",
  "alg": "HS256"
}.
{
  "iss": "Some lite service",
  "iat": 1507542376,
  "exp": 1602236776,
  "aud": "lite",
  "sub": "123456",
  "email": "example@example.com"
}
```
With a shared secret of "demo-secret-which-is-very-long-so-as-to-hit-the-byte-requirement" 
