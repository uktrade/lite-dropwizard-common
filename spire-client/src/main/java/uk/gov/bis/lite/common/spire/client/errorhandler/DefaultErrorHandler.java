package uk.gov.bis.lite.common.spire.client.errorhandler;

import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;

/**
 * DefaultErrorHandler
 * Throws a SpireClientException
 */
public class DefaultErrorHandler extends AbstractErrorHandler {

  public DefaultErrorHandler() {}

  public void mapErrorText(String errorText) {
    throw new SpireClientException("ERROR: [" + errorText + "]");
  }

}
