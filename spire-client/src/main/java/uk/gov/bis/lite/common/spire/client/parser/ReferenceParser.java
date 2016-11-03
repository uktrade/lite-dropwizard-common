package uk.gov.bis.lite.common.spire.client.parser;

import uk.gov.bis.lite.common.spire.client.SpireResponse;

public class ReferenceParser implements SpireParser<String> {

  private String referenceElementName;

  public ReferenceParser(String referenceElementName) {
    this.referenceElementName = referenceElementName;
  }

  @Override
  public String parseResponse(SpireResponse spireResponse) {
    return spireResponse.getResponseElementContent(referenceElementName);
  }

}
