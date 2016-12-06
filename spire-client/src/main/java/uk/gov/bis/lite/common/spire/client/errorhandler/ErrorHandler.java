package uk.gov.bis.lite.common.spire.client.errorhandler;

import uk.gov.bis.lite.common.spire.client.SpireResponse;

/**
 * Spire error handling interface
 */
public interface ErrorHandler {

  void checkResponse(SpireResponse spireResponse);

}
