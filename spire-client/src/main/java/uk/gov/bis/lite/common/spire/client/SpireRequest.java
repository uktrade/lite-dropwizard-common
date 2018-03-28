package uk.gov.bis.lite.common.spire.client;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * SpireClient request
 */
public class SpireRequest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireRequest.class);

  private final SOAPMessage message;
  private final SOAPElement parent;

  /**
   * A SpireRequest wraps a SOAPMessage
   *
   * @param message SOAPMessage {@link SOAPMessage}
   */
  SpireRequest(SOAPMessage message) {
    this.message = message;
    this.parent = getParent(message);
  }

  private SOAPElement getParent(SOAPMessage soapMessage) {
    try {
      return (SOAPElement) soapMessage.getSOAPPart().getEnvelope().getBody().getChildElements().next();
    } catch (SOAPException e) {
      LOGGER.error("Unable to get parent of soap message", e);
      return null;
    }
  }

  /**
   * Add a child element to soap body only if the element context is not null or empty
   * <p>
   * Example:
   * <pre>
   * {@code
   *  <SAR_REF>SAR17371</SAR_REF>
   * }
   * </pre>
   *
   * @param childName name of child element ('SAR_REF')
   * @param childText content of child element ('SAR17371')
   */
  public void addChild(String childName, String childText) {
    try {
      if (!StringUtils.isBlank(childText)) {
        SOAPElement child = parent.addChildElement(childName);
        child.addTextNode(childText);
        message.saveChanges();
      }
    } catch (SOAPException e) {
      throw new SpireClientException("An error occurred adding child element", e);
    }
  }

  /**
   * Add a child list element structure with a single element content.
   * <p>
   * Example:
   * <pre>
   * {@code
   *  <OGL_TYPE_LIST>
   *    <OGL_TYPE>
   *      <TYPE>OGL1</TYPE>
   *    </OGL_TYPE>
   *  </OGL_TYPE_LIST>
   * }
   * </pre>
   *
   * @param listName    name of list element ('OGL_TYPE_LIST')
   * @param elementName name of element ('OGL_TYPE')
   * @param childName   name of child element ('TYPE')
   * @param childText   content of child element('OGL1')
   */
  public void addChildList(String listName, String elementName, String childName, String childText) {
    try {
      SOAPElement list = parent.addChildElement(listName);
      SOAPElement element = list.addChildElement(elementName);
      SOAPElement child = element.addChildElement(childName);
      child.addTextNode(childText);
      message.saveChanges();
    } catch (SOAPException e) {
      throw new SpireClientException("An error occurred adding child element", e);
    }
  }

  /**
   * Returns wrapped SOAPMessage
   *
   * @return SOAPMessage
   */
  public SOAPMessage getSoapMessage() {
    return message;
  }

}
