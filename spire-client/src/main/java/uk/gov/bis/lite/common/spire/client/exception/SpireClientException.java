package uk.gov.bis.lite.common.spire.client.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class SpireClientException extends WebApplicationException {

  /**
   * SpireClientException
   *
   * @param info information on exception
   */
  public SpireClientException(String info) {
    super("Spire Client Exception: " + info, Response.Status.BAD_REQUEST);
  }

  /**
   * SpireClientException
   *
   * @param info  information on exception
   * @param cause the cause
   */
  public SpireClientException(String info, Throwable cause) {
    super("Spire Client Exception: " + info, cause, Response.Status.BAD_REQUEST);
  }

}
