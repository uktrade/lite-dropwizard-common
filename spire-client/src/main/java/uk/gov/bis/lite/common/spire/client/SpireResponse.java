package uk.gov.bis.lite.common.spire.client;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * SpireClient response
 */
public class SpireResponse {

  private static final String XPATH_EXP_RESPONSE = "//*[local-name()='RESPONSE']";

  private static final XPath xpath = XPathFactory.newInstance().newXPath();

  private final SOAPMessage message;

  public SpireResponse(SOAPMessage message) {
    this.message = message;
  }

  /**
   * Returns content of response element
   *
   * @param referenceElementName the name of the response element
   * @return content derived from names response element
   */
  public String getResponseElementContent(String referenceElementName) {
    List<Node> nodes = getResponseElementNodes();
    return reduce(nodes, referenceElementName);
  }

  /**
   * Returns list of immediate child Nodes of named list element
   *
   * @param listElementName the named list element
   * @return list of named list element child nodes
   */
  public List<Node> getElementChildNodesForList(String listElementName) {
    return getChildrenOfBodyNodes(listElementName);
  }

  private List<Node> getResponseElementNodes() {
    try {
      NodeList nodeList = (NodeList) xpath.evaluate(XPATH_EXP_RESPONSE, message.getSOAPBody(), XPathConstants.NODESET);
      if (nodeList != null && nodeList.item(0) != null) {
        return list(nodeList.item(0).getChildNodes());
      } else {
        return new ArrayList<>();
      }
    } catch (SOAPException | XPathExpressionException e) {
      throw new SpireClientException("An error occurred while extracting the SOAP Response Body", e);
    }
  }

  private List<Node> getChildrenOfBodyNodes(String xpathExpression) {
    List<Node> nodes = new ArrayList<>();
    try {
      NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, message.getSOAPBody(), XPathConstants.NODESET);
      list(nodeList).stream().filter(Node::hasChildNodes).forEach(node -> {
        nodes.addAll(list(node.getChildNodes()));
      });
    } catch (SOAPException | XPathExpressionException e) {
      throw new SpireClientException("An error occurred while extracting the SOAP Response Body", e);
    }
    return nodes;
  }

  public static Optional<String> getNodeValue(Node singleNode, String name) {
    try {
      Node node = (Node) xpath.evaluate(name, singleNode, XPathConstants.NODE);
      if (node != null) {
        return Optional.of(node.getTextContent());
      }
    } catch (XPathExpressionException e) {
      throw new SpireClientException("Error occurred while parsing the SOAP response body", e);
    }
    return Optional.empty();
  }

  public static List<Node> getChildrenOfChildNode(Node parent, String childName) {
    List<Node> nodes = new ArrayList<>();
    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      Node child = (Node) xpath.evaluate(childName, parent, XPathConstants.NODE);
      if (child != null) {
        nodes = list(child.getChildNodes());
      }
    } catch (XPathExpressionException e) {
      throw new SpireClientException("An error occurred while extracting the SOAP Response Body", e);
    }
    return nodes;
  }

  public SOAPMessage getMessage() {
    return message;
  }

  private static String reduce(List<Node> nodes, String nodeName) {
    return nodes.stream()
        .filter(node -> node.getNodeName().equals(nodeName))
        .map(SpireResponse::getText)
        .collect(Collectors.joining());
  }

  private static List<Node> list(NodeList nodeList) {
    return nodeList != null ? IntStream.range(0, nodeList.getLength())
        .mapToObj(nodeList::item)
        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
        .collect(Collectors.toList()) : new ArrayList<>();
  }

  private static String getText(Node node) {
    StringBuilder reply = new StringBuilder();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if ((isCharacterData(child) && !isComment(child)) || isEntityReference(child)) {
        reply.append(child.getNodeValue());
      } else if (isElementNode(child)) {
        reply.append(getText(child));
      }
    }
    return reply.toString();
  }

  private static boolean isEntityReference(Node node) {
    return node instanceof EntityReference;
  }

  private static boolean isComment(Node node) {
    return node instanceof Comment;
  }

  private static boolean isCharacterData(Node node) {
    return node instanceof CharacterData;
  }

  private static boolean isElementNode(Node node) {
    return node.getNodeType() == Node.ELEMENT_NODE;
  }

}
