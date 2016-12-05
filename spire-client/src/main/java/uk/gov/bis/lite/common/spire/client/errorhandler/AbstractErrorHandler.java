package uk.gov.bis.lite.common.spire.client.errorhandler;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.common.spire.client.SpireResponse;

import java.util.Optional;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Extend this class and implement mapErrorText method to define custom error handling
 */
public abstract class AbstractErrorHandler implements ErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractErrorHandler.class);
  private static final String ERROR = "ERROR";
  private static final String XPATH_EXP_RESPONSE = "//*[local-name()='RESPONSE']";
  private static final XPath xpath = XPathFactory.newInstance().newXPath();

  public abstract void mapErrorText(String errorText);

  /**
   * Checks for any ERROR node text and delegates calls child implementing class mapErrorText method
   * @param spireResponse
   */
  public void checkResponse(SpireResponse spireResponse) {
    Optional<String> optErrorText = getErrorTextContent(spireResponse.getMessage());
    if(optErrorText.isPresent()) {
      mapErrorText(optErrorText.get());
    }
  }

  public boolean failOnSoapFault() {
    return true;
  }

  /**
   * Looks for an ERROR node, and returns any textual content of found
   */
  private Optional<String> getErrorTextContent(SOAPMessage message) {
    try {
      NodeList responseNodes = (NodeList) xpath.evaluate(XPATH_EXP_RESPONSE, message.getSOAPBody(), XPathConstants.NODESET);
      if (responseNodes != null) {
        Node first = responseNodes.item(0);
        if (first != null) {
          NodeList nodes = first.getChildNodes();
          Node errorNode = (Node) XPathFactory.newInstance().newXPath().evaluate(ERROR, nodes, XPathConstants.NODE);
          if (errorNode != null) {
            return Optional.of(errorNode.getTextContent());
          }
        }
      }
    } catch (XPathExpressionException | SOAPException e) {
      LOGGER.warn("Exception: " + Throwables.getStackTraceAsString(e));
    }
    return Optional.empty();
  }
}
