package uk.gov.bis.lite.common.spire.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireRequestTest {

  private static final String SOAP_START = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/>";
  private static final String SOAP_END = "</SOAP-ENV:Envelope>";
  
  @Test
  public void shouldAddChild() throws SOAPException, IOException {
    // Setup new spire request with soap body <getEntries/>
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    message.getSOAPPart().getEnvelope().getBody().addChildElement("getEntries");
    SpireRequest spireRequest = new SpireRequest(message);

    // Add child to request
    spireRequest.addChild("SAR_REF", "SAR17371");

    // Verify that request contains child
    String body = "<SOAP-ENV:Body><getEntries><SAR_REF>SAR17371</SAR_REF></getEntries></SOAP-ENV:Body>";
    String expected = SOAP_START + body + SOAP_END;
    assertEquals(expected, getXml(spireRequest.getSoapMessage()));
  }

  @Test
  public void shouldAddChildList() throws SOAPException, IOException {
    // Setup new spire request with soap body <getEntries/>
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    message.getSOAPPart().getEnvelope().getBody().addChildElement("getEntries");
    SpireRequest spireRequest = new SpireRequest(message);

    // Add child list to request
    spireRequest.addChildList("OGL_TYPE_LIST", "OGL_TYPE", "TYPE", "OGL1");

    // Verify that request contains child list
    String body = "<SOAP-ENV:Body><getEntries><OGL_TYPE_LIST><OGL_TYPE><TYPE>OGL1</TYPE></OGL_TYPE></OGL_TYPE_LIST></getEntries></SOAP-ENV:Body>";
    String expected = SOAP_START + body + SOAP_END;
    assertEquals(expected, getXml(spireRequest.getSoapMessage()));
  }

  private String getXml(SOAPMessage message) throws IOException, SOAPException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    message.writeTo(out);
    return out.toString();
  }

}
