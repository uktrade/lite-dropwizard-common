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
 * ErrorNodeErrorHandler
 * <p>
 * Extend this class and implement handleError(String errorText) method to define custom error handling
 * <p>
 * As an example:
 * <p>
 * {@code
 * <ns:RESPONSE>
 * <ERROR>Could not create SAR. The error has been logged.</ERROR>
 * </ns:RESPONSE>
 * }
 * <p>
 * This results int a call to handleError(String errorText) with ERROR element content as errorText
 */
public abstract class ErrorNodeErrorHandler implements ErrorHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ErrorNodeErrorHandler.class);

  private static final String DEFAULT_ERROR_NODE_NAME = "ERROR";
  private static final String DEFAULT_RESPONSE_NODE_XPATH = "//*[local-name()='RESPONSE']";

  private final String errorNodeName;
  private final String responseNodeXpath;

  public ErrorNodeErrorHandler(String responseNodesXpath, String errorNodeName) {
    this.errorNodeName = errorNodeName;
    this.responseNodeXpath = responseNodesXpath;
  }

  public ErrorNodeErrorHandler(String responseNodesXpath) {
    this(responseNodesXpath, DEFAULT_ERROR_NODE_NAME);
  }

  public ErrorNodeErrorHandler() {
    this(DEFAULT_RESPONSE_NODE_XPATH, DEFAULT_ERROR_NODE_NAME);
  }

  public abstract void handleError(String errorText);

  /**
   * Checks for any ERROR node text and delegates calls child implementing class mapErrorText method
   *
   * @param spireResponse wrapped SOAPMessage
   */
  public void checkResponse(SpireResponse spireResponse) {
    Optional<String> optErrorText = getErrorTextContent(spireResponse.getMessage());
    if (optErrorText.isPresent()) {
      handleError(optErrorText.get());
    }
  }

  /**
   * Looks for an ERROR node, and returns any textual content of found
   */
  private Optional<String> getErrorTextContent(SOAPMessage message) {
    XPath xpath = XPathFactory.newInstance().newXPath();
    try {
      NodeList responseNodes = (NodeList) xpath.evaluate(responseNodeXpath, message.getSOAPBody(), XPathConstants.NODESET);
      if (responseNodes != null) {
        Node first = responseNodes.item(0);
        if (first != null) {
          NodeList nodes = first.getChildNodes();
          Node errorNode = (Node) XPathFactory.newInstance().newXPath().evaluate(errorNodeName, nodes, XPathConstants.NODE);
          if (errorNode != null) {
            return Optional.of(errorNode.getTextContent());
          }
        }
      }
    } catch (XPathExpressionException | SOAPException e) {
      LOGGER.warn("Exception: {}", Throwables.getStackTraceAsString(e));
    }
    return Optional.empty();
  }
}
