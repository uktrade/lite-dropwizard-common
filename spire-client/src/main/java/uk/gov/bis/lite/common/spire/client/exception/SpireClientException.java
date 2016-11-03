package uk.gov.bis.lite.common.spire.client.exception;

import io.dropwizard.jersey.errors.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class SpireClientException extends RuntimeException {

  /**
   * SpireClientException
   * @param info information on exception
   */
  public SpireClientException(String info) {
    super("Spire Client Exception: " + info);
  }

  /**
   * Provided for Dropwizard/Jersey integration
   */
  public static class ServiceExceptionMapper implements ExceptionMapper<SpireClientException> {

    @Override
    public Response toResponse(SpireClientException exception) {
      return Response.status(400).entity(new ErrorMessage(400, exception.getMessage())).build();
    }
  }
}
