package uk.gov.bis.lite.common.spire.client;

import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

/**
 * SpireClient request
 */
public class SpireRequest {

  private SOAPMessage message;
  private SOAPElement parent;

  /**
   * A SpireRequest wraps a SOAPMessage
   *
   * @param message SOAPMessage {@link SOAPMessage}
   */
  public SpireRequest(SOAPMessage message) {
    this.message = message;
    try {
      this.parent = (SOAPElement) message.getSOAPPart().getEnvelope().getBody().getChildElements().next();
    } catch (SOAPException e) {
      e.printStackTrace();
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
      throw new RuntimeException("An error occurred adding child element", e);
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
      throw new RuntimeException("An error occurred adding child element", e);
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
