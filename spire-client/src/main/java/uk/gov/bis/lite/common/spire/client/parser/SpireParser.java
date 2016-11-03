package uk.gov.bis.lite.common.spire.client.parser;


import uk.gov.bis.lite.common.spire.client.SpireResponse;

/**
 * A SpireClient requires a parser that implements this interface
 * @param <T> generic type parameter
 */
public interface SpireParser<T> {

  /**
   * Parse response to extract generic type data
   *
   * @param spireResponse spire client response
   * @return generic type parameter
   */
  T parseResponse(SpireResponse spireResponse);

}
