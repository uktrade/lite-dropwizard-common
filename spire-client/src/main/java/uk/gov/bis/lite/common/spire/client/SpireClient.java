package uk.gov.bis.lite.common.spire.client;


import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.errorhandler.DefaultErrorHandler;
import uk.gov.bis.lite.common.spire.client.errorhandler.AbstractErrorHandler;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
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
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;

/**
 * SpireClient
 * Used to call Spire and extract data
 *
 * @param <T> generic type parameter
 */
public class SpireClient<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(SpireClient.class);

  private final String NAMESPACE_URI = "http://www.fivium.co.uk/fox/webservices/ispire/";
  private final String SPIR_PREFIX = "spir";

  private final SpireParser<T> parser;

  private final String namespace;
  private final String requestChildName;
  private final boolean useSpirePrefix;
  private final String username;
  private final String password;
  private final String url;
  private final AbstractErrorHandler errorHandler;

  /**
   * SpireClient
   *
   * @param parser        a client specific parser implements SpireParser interface {@link SpireParser}
   * @param clientConfig  spire connection details
   * @param requestConfig configuration relating to specific Client soap endpoint
   */
  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig) {
    this.parser = parser;
    this.username = clientConfig.getUsername();
    this.password = clientConfig.getPassword();
    this.url = clientConfig.getUrl();
    this.namespace = requestConfig.getNamespace();
    this.requestChildName = requestConfig.getRequestChildName();
    this.useSpirePrefix = requestConfig.isUseSpirePrefix();
    this.errorHandler = new DefaultErrorHandler();
  }

  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig,
                     SpireRequestConfig requestConfig, AbstractErrorHandler errorHandler) {
    this.parser = parser;
    this.username = clientConfig.getUsername();
    this.password = clientConfig.getPassword();
    this.url = clientConfig.getUrl();
    this.namespace = requestConfig.getNamespace();
    this.requestChildName = requestConfig.getRequestChildName();
    this.useSpirePrefix = requestConfig.isUseSpirePrefix();
    this.errorHandler = errorHandler;
  }

  /**
   * Make a client call to Spire
   *
   * @param request request created by SpireClient
   * @return generic type parameter
   */
  public T sendRequest(SpireRequest request) {
    SpireResponse spireResponse = getSpireResponse(request, namespace);
    if(errorHandler.failOnSoapFault()) {
      throwSoapFaultSpireException(spireResponse.getMessage());
    }
    errorHandler.checkResponse(spireResponse);
    return parser.parseResponse(spireResponse);
  }

  /**
   * Create a SpireRequest
   *
   * @return SpireRequest
   */
  public SpireRequest createRequest() {
    return new SpireRequest(createRequestSoapMessage(namespace, requestChildName, useSpirePrefix));
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

  private void throwSoapFaultSpireException(SOAPMessage message) {
    String faultString = "";
    try {
      SOAPFault fault = message.getSOAPBody().getFault();
      if (fault != null) {
        faultString = fault.getFaultString();
      }
    } catch (SOAPException e) {
      LOGGER.warn("Exception: " + Throwables.getStackTraceAsString(e));
    }
    if (!StringUtils.isBlank(faultString)) {
      throw new SpireClientException("soap:Fault: [" + faultString + "]");
    }
  }

  private void logSoapMessage(String type, SOAPMessage message) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      message.writeTo(out);
      LOGGER.trace(type + ": " + out.toString());
    } catch (IOException | SOAPException e) {
      LOGGER.error("error", e);
    }
  }

}
