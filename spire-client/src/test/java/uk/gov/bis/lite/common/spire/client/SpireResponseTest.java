package uk.gov.bis.lite.common.spire.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPMessage;

public class SpireResponseTest {

  private static final String XML_STRING = doubleEscape("<?xml version='1.0' encoding='UTF-16'?>\\n");
  private static final String QUOTE = "\"";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void shouldGetResponseElementContent() throws SOAPException {
    // Setup SOAP message with three text nodes
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPBody body = message.getSOAPPart().getEnvelope().getBody();

    SOAPElement responseElement = body.addChildElement("RESPONSE");
    SOAPElement firstChild = responseElement.addChildElement("CHILD-1");
    firstChild.addTextNode("TEXT-1");
    SOAPElement secondChild = firstChild.addChildElement("CHILD-2");
    secondChild.addTextNode("TEXT-2");
    secondChild.addTextNode("TEXT-3");

    // Create new spireResponse
    SpireResponse spireResponse = new SpireResponse(message);

    // Verify that getResponseElementContent returns the content of all text nodes
    String responseElementContent = spireResponse.getResponseElementContent("CHILD-1");
    assertThat(responseElementContent).isEqualTo("TEXT-1TEXT-2TEXT-3");
  }

  @Test
  public void shouldGetEmptyStringFor_GetResponseElementContent_IfElementDoesNotExist() throws SOAPException {
    // Setup SOAP message with body
    // <RESPONSE/>
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPBody body = message.getSOAPPart().getEnvelope().getBody();
    body.addChildElement("RESPONSE");

    // Create new spireResponse
    SpireResponse spireResponse = new SpireResponse(message);

    // Verify that getResponseElementContent is empty
    String elementContent = spireResponse.getResponseElementContent("MADE-UP");
    assertThat(elementContent).isEmpty();
  }

  @Test
  public void shouldGetElementChildNodesForList() throws SOAPException, JsonProcessingException {
    // Setup SOAP message with body
    // <RESPONSE>
    //   <ENTRY_LIST>
    //     <ENTRY>
    //       <ENTRY-NAME>entry-1</ENTRY-NAME>
    //     </ENTRY>
    //     <ENTRY>
    //       <ENTRY-NAME>entry-2</ENTRY-NAME>
    //     </ENTRY>
    //   </ENTRY_LIST>
    // </RESPONSE>
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPBody body = message.getSOAPPart().getEnvelope().getBody();
    SOAPElement responseElement = body.addChildElement("RESPONSE");
    SOAPElement entryList = responseElement.addChildElement("ENTRY_LIST");
    for (String name : Arrays.asList("entry-1", "entry-2")) {
      SOAPElement entry = entryList.addChildElement("ENTRY");
      SOAPElement entryName = entry.addChildElement("ENTRY-NAME");
      entryName.addTextNode(name);
    }

    // Create new spireResponse
    SpireResponse spireResponse = new SpireResponse(message);

    // Verify that getElementChildNodesForList contains both entries
    List<Node> nodes = spireResponse.getElementChildNodesForList("//ENTRY_LIST");
    assertThat(nodes).hasSize(2);
    Node nodeOne = nodes.get(0);
    Node nodeTwo = nodes.get(1);

    String EXPECTED_NODE_ONE = "<ENTRY><ENTRY-NAME>entry-1</ENTRY-NAME></ENTRY>";
    String EXPECTED_NODE_TWO = "<ENTRY><ENTRY-NAME>entry-2</ENTRY-NAME></ENTRY>";

    assertThat(MAPPER.writeValueAsString(nodeOne)).isEqualTo(QUOTE + XML_STRING + EXPECTED_NODE_ONE + QUOTE);
    assertThat(MAPPER.writeValueAsString(nodeTwo)).isEqualTo(QUOTE + XML_STRING + EXPECTED_NODE_TWO + QUOTE);
  }

  @Test
  public void shouldReturnEmptyListFor_GetElementChildNodesForList_IfNodeDoesNotExist() throws SOAPException {
    // Setup SOAP message with body
    // <RESPONSE/>
    SOAPMessage message = MessageFactory.newInstance().createMessage();
    SOAPBody body = message.getSOAPPart().getEnvelope().getBody();
    body.addChildElement("RESPONSE");

    // Create new spireResponse
    SpireResponse spireResponse = new SpireResponse(message);

    // Verify that getElementChildNodesForList returns empty list for made-up node
    List<Node> nodes = spireResponse.getElementChildNodesForList("//MADE-UP");
    assertThat(nodes).hasSize(0);
  }

  @Test
  public void shouldGetNodeValue() throws SOAPException {
    // Create xml
    //  <ENTRY>
    //    <ENTRY-NAME>entry-1</ENTRY-NAME>
    //  </ENTRY>
    SOAPElement entry = SOAPFactory.newInstance().createElement("ENTRY");
    SOAPElement entryName = entry.addChildElement("ENTRY-NAME");
    entryName.addTextNode("entry-1");

    // Verify that the static method getNodeValue returns the correct value for "ENTRY_NAME"
    Optional<String> name = SpireResponse.getNodeValue(entry, "ENTRY-NAME");
    assertThat(name).hasValue("entry-1");
  }

  @Test
  public void shouldReturnEmptyOptionalFor_GetNodeValue_IfNodeDoesNotExist() throws SOAPException {
    // Create xml
    // <ENTRY/>
    Node entry = SOAPFactory.newInstance().createElement("ENTRY");

    // Verify that the static method getNodeValue returns an empty optional for "MADE-UP"
    Optional<String> madeUp = SpireResponse.getNodeValue(entry, "MADE-UP");
    assertThat(madeUp).isEmpty();
  }

  @Test
  public void shouldGetChildrenOfChildNode() throws SOAPException, JsonProcessingException {
    // Create xml
    // <COMPANY>
    //   <WEBSITE_LIST>
    //     <WEBSITE>
    //       <WEBSITE-NAME>website-1</WEBSITE-NAME>
    //     </WEBSITE>
    //     <WEBSITE>
    //       <WEBSITE-NAME>website-2</WEBSITE-NAME>
    //     </WEBSITE>
    //   </WEBSITE_LIST>
    // </COMPANY>
    SOAPElement company = SOAPFactory.newInstance().createElement("COMPANY");
    SOAPElement websiteList = company.addChildElement("WEBSITE_LIST");
    for (String name : Arrays.asList("website-1", "website-2")) {
      SOAPElement website = websiteList.addChildElement("WEBSITE");
      SOAPElement websiteName = website.addChildElement("WEBSITE-NAME");
      websiteName.addTextNode(name);
    }

    // Verify that the static method getChildrenOfChildNode returns websites of company
    List<Node> websites = SpireResponse.getChildrenOfChildNode(company, "WEBSITE_LIST");

    assertThat(websites).hasSize(2);

    String EXPECTED_WEBSITE_ONE = "<WEBSITE><WEBSITE-NAME>website-1</WEBSITE-NAME></WEBSITE>";
    String EXPECTED_WEBSITE_TWO = "<WEBSITE><WEBSITE-NAME>website-2</WEBSITE-NAME></WEBSITE>";
    assertThat(MAPPER.writeValueAsString(websites.get(0))).isEqualTo(QUOTE + XML_STRING + EXPECTED_WEBSITE_ONE + QUOTE);
    assertThat(MAPPER.writeValueAsString(websites.get(1))).isEqualTo(QUOTE + XML_STRING + EXPECTED_WEBSITE_TWO + QUOTE);
  }

  @Test
  public void shouldReturnEmptyListFor_GetChildrenOfChildNode_IfNodeDoesNotExist() throws SOAPException {
    // Create xml
    // <COMPANY/>
    SOAPElement company = SOAPFactory.newInstance().createElement("COMPANY");

    // Verify that the static method getChildrenOfChildNode returns an empty list for a made-up node
    List<Node> nodes = SpireResponse.getChildrenOfChildNode(company, "MADE-UP");
    assertThat(nodes).isEmpty();
  }

  private static String doubleEscape(String str) {
    return str.replace("'", "\\\"");
  }

}
