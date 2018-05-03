package app;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.auth.Auth;
import uk.gov.bis.lite.common.jwt.LiteJwtUser;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class TestResource {

  @GET
  @Produces({MediaType.TEXT_PLAIN})
  @Path("/noauth")
  public String noauth() {
    return "noauth";
  }

  @GET
  @Produces({MediaType.APPLICATION_JSON})
  @Path("/auth")
  public String auth(@Auth LiteJwtUser liteJwtUser) {
    final JsonNodeFactory factory = JsonNodeFactory.instance;
    ObjectNode objectNode = factory.objectNode();
    objectNode.put("name", liteJwtUser.getName());
    objectNode.put("userId", liteJwtUser.getUserId());
    objectNode.put("email", liteJwtUser.getEmail());
    objectNode.put("fullName", liteJwtUser.getFullName());
    objectNode.put("accountType", liteJwtUser.getAccountType().toString());
    return objectNode.toString();
  }
}
