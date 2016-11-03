package uk.gov.bis.lite.common.spire.client.parser;


import uk.gov.bis.lite.common.spire.client.SpireResponse;

public interface SpireParser<T> {

  T parseResponse(SpireResponse spireResponse);

}
