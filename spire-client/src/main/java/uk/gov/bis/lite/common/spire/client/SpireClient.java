package uk.gov.bis.lite.common.spire.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

public class SpireClient<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClient.class);

  private final String NAMESPACE_URI = "http://www.fivium.co.uk/fox/webservices/ispire/";
  private final String SPIR_PREFIX = "spir";

  private final SpireParser<T> parser;

  private final String nameSpace;
  private final String requestChildName;
  private final boolean useSpirePrefix;
  private final String username;
  private final String password;
  private final String url;

  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig) {
    this.parser = parser;
    this.username = clientConfig.getUsername();
    this.password = clientConfig.getPassword();
    this.url = clientConfig.getUrl();
    this.nameSpace = requestConfig.getNameSpace();
    this.requestChildName = requestConfig.getRequestChildName();
    this.useSpirePrefix = requestConfig.isUseSpirePrefix();
  }

  public T sendRequest(SpireRequest request) {
    SpireResponse spireResponse = getSpireResponse(request, nameSpace);

    // Check for SoapResponse Errors - throws SpireClientException if found
    spireResponse.checkForErrors();

    return parser.parseResponse(spireResponse);
  }

  public SpireRequest createRequest() {
    return new SpireRequest(createRequestSoapMessage(nameSpace, requestChildName, useSpirePrefix));
  }

  private SpireResponse getSpireResponse(SpireRequest request, String urlSuffix) {
    logSoapMessage("request", request.getSoapMessage());
    SOAPMessage response = doExecuteRequest(request, urlSuffix);
    logSoapMessage("response", response);
    return new SpireResponse(response);
  }

  private SOAPMessage createRequestSoapMessage(String namespace, String childName, boolean withSpirPrefix) {
    try {
      SOAPMessage message = MessageFactory.newInstance().createMessage();
      message.getSOAPPart().getEnvelope().addNamespaceDeclaration(SPIR_PREFIX, NAMESPACE_URI + namespace);

      SOAPBody soapBody = message.getSOAPPart().getEnvelope().getBody();
      if (withSpirPrefix) {
        soapBody.addChildElement(childName, SPIR_PREFIX);
      } else {
        soapBody.addChildElement(childName);
      }
      addAuthorizationHeader(message);
      message.saveChanges();
      return message;
    } catch (SOAPException | UnsupportedEncodingException e) {
      throw new RuntimeException("Error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    }
  }

  private void addAuthorizationHeader(SOAPMessage message) throws UnsupportedEncodingException {
    MimeHeaders headers = message.getMimeHeaders();
    String authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("utf-8"));
    headers.addHeader("Authorization", "Basic " + authorization);
  }

  private SOAPMessage doExecuteRequest(SpireRequest request, String urlSuffix) {
    SOAPConnection conn = null;
    try {
      conn = SOAPConnectionFactory.newInstance().createConnection();
      return conn.call(request.getSoapMessage(), url + urlSuffix);
    } catch (SOAPException e) {
      throw new RuntimeException("Error occurred establishing connection with SOAP client", e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SOAPException e) {
          LOGGER.error("Error occurred closing SOAP connection. ", e);
        }
      }
    }
  }

  private void logSoapMessage(String type, SOAPMessage message) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      LOGGER.info(type + ": " + out.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }

}
