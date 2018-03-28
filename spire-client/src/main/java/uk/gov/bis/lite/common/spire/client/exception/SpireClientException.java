package uk.gov.bis.lite.common.spire.client.exception;

public class SpireClientException extends RuntimeException {

  /**
   * SpireClientException
   *
   * @param info information on exception
   */
  public SpireClientException(String info) {
    super("Spire Client Exception: " + info);
  }

  /**
   * SpireClientException
   *
   * @param info  information on exception
   * @param cause the cause
   */
  public SpireClientException(String info, Throwable cause) {
    super("Spire Client Exception: " + info, cause);
  }

}
