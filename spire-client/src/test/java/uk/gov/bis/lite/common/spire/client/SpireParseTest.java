package uk.gov.bis.lite.common.spire.client;


import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * Testing Spire Client Parsers
 */
public class SpireParseTest {

  @Test
  public void testReferenceParser() {
    String sarRef = new ReferenceParser("SAR_REF").parseResponse(createSpireResponse("sarRef.xml"));
    assertThat(sarRef).isEqualTo("SAR1");

    String siteRef = new ReferenceParser("SITE_REF").parseResponse(createSpireResponse("siteRef.xml"));
    assertThat(siteRef).isEqualTo("SITE1");

    String spireRef = new ReferenceParser("SPIRE_REF").parseResponse(createSpireResponse("spireRef.xml"));
    assertThat(spireRef).isEqualTo("SPIRE1");

  }

  private SpireResponse createSpireResponse(String fileName) {
    return new SpireResponse(getSoapMessage(fileName));
  }

  private SOAPMessage getSoapMessage(String fileName) {
    return createSoapMessage(readResource(fileName, Charsets.UTF_8));
  }

  private String readResource(String fileName, Charset charset) {
    String fileContent = "";
    try {
      fileContent = Resources.toString(Resources.getResource(fileName), charset);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return fileContent;
  }

  private SOAPMessage createSoapMessage(String xml) {
    SOAPMessage message = null;
    try {
      message = MessageFactory.newInstance().createMessage(null, new ByteArrayInputStream(xml.getBytes()));
    } catch (IOException | SOAPException e) {
      e.printStackTrace();
    }
    return message;
  }

}