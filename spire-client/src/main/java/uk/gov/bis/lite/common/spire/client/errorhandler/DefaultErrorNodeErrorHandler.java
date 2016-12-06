package uk.gov.bis.lite.common.spire.client.errorhandler;

import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;

/**
 * DefaultErrorNodeErrorHandler
 * Default behaviour to throw a SpireClientException which contains the errorText.
 */
public class DefaultErrorNodeErrorHandler extends ErrorNodeErrorHandler {

  public DefaultErrorNodeErrorHandler() {}

  public void handleError(String errorText) {
    throw new SpireClientException("ERROR: [" + errorText + "]");
  }

}
