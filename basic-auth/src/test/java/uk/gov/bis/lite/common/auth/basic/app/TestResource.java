package uk.gov.bis.lite.common.auth.basic.app;

import io.dropwizard.auth.Auth;
import uk.gov.bis.lite.common.auth.basic.Roles;
import uk.gov.bis.lite.common.auth.basic.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/auth")
public class TestResource {

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
