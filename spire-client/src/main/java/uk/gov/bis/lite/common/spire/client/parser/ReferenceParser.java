package uk.gov.bis.lite.common.spire.client.parser;

import uk.gov.bis.lite.common.spire.client.SpireResponse;

/**
 * Parses a single response element for its content
 * Returns content as String
 * ReferenceParser implements SpireParser {@link SpireParser}
 */
public class ReferenceParser implements SpireParser<String> {

  private final String referenceElementName;

  /**
   * @param referenceElementName name of single element
   */
  public ReferenceParser(String referenceElementName) {
    this.referenceElementName = referenceElementName;
  }

  @Override
  public String parseResponse(SpireResponse spireResponse) {
    return spireResponse.getResponseElementContent(referenceElementName);
  }

}
