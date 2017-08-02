package uk.gov.bis.lite.common.spire.client.parser;


import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.SpireResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Testing Spire Client Parsers
 */
public class ReferenceParserTest {

  @Test
  public void testReferenceParser() throws SOAPException, IOException {
    String sarRef = new ReferenceParser("SAR_REF").parseResponse(createSpireResponse("sarRef.xml"));
    assertThat(sarRef).isEqualTo("SAR1");

    String siteRef = new ReferenceParser("SITE_REF").parseResponse(createSpireResponse("siteRef.xml"));
    assertThat(siteRef).isEqualTo("SITE1");

    String spireRef = new ReferenceParser("SPIRE_REF").parseResponse(createSpireResponse("spireRef.xml"));
    assertThat(spireRef).isEqualTo("SPIRE1");

    String madeUpRef = new ReferenceParser("MADE_UP").parseResponse(createSpireResponse("spireRef.xml"));
    assertThat(madeUpRef).isEmpty();
  }

  private SpireResponse createSpireResponse(String fileName) throws SOAPException, IOException {
    return new SpireResponse(getSoapMessage(fileName));
  }

  private SOAPMessage getSoapMessage(String fileName) throws SOAPException, IOException {
    return createSoapMessage(fixture(fileName));
  }

  private SOAPMessage createSoapMessage(String xml) throws SOAPException, IOException {
    return MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes()));
  }

}
