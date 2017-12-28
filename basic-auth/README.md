# Basic Auth

Helpers for running a service with basic authentication for a service and an admin user.

## Getting started

Add the dependency to your `build.gradle`:

```groovy
compile 'uk.gov.bis.lite:basic-auth:1.0'
```

## Usage

In your `Configuration` class, add the following:

```java
public class MyConfiguration extends Configuration {
  ...
  @NotEmpty
  @JsonProperty
  private String adminLogin;

  @NotEmpty
  @JsonProperty
  private String adminPassword;

  @NotEmpty
  @JsonProperty
  private String serviceLogin;

  @NotEmpty
  @JsonProperty
  private String servicePassword;

  public String getAdminLogin() {
    return adminLogin;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public String getServiceLogin() {
    return serviceLogin;
  }

  public String getServicePassword() {
    return servicePassword;
  }
  ...
}
```

In your Dropwizard `Application` class, add the following to the `run` method

```java
public class MyApplication extends Application<MyConfiguration> {
  ...
  @Override
  public void run(MyConfiguration configuration, Environment environment) throws Exception {
    ...
    //Authorization and authentication handlers
    SimpleAuthenticator simpleAuthenticator = new SimpleAuthenticator(configuration.getAdminLogin(),
        configuration.getAdminPassword(),
        configuration.getServiceLogin(),
        configuration.getServicePassword());
    environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
        .setAuthenticator(simpleAuthenticator)
        .setAuthorizer(new SimpleAuthorizer())
        .setRealm("My Service Authentication")
        .buildAuthFilter()));
    environment.jersey().register(RolesAllowedDynamicFeature.class);
    environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));
    ...
  }

}
```

Subsequently, add the annotation `@RolesAllowed(Roles.SERVICE)` or `@RolesAllowed(Roles.ADMIN)` to any resource method which requires authorisation.
`@RolesAllowed(Roles.SERVICE)` will allow both the service and the admin user to make requests.
`@RolesAllowed(Roles.ADMIN)` will allow only the admin user to make requests.   
In addition, it's recommended to add the parameter `@Auth User user` to all methods that require service or admin login,
even if the `User` object is not referenced.
This ensures that if a programmer would accidently remove a `@RolesAllowed` annotation,
the resource method would still require admin or service user login.

```java
public class MyResource {

  @RolesAllowed(Roles.SERVICE)
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response get(@Auth User user) {
    return Response.ok().build();
  }

  @RolesAllowed(Roles.ADMIN)
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response delete(@Auth User user) {
    return Response.ok().build();
  }

}
```
