package uk.gov.bis.lite.common.spire.client;


import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.bis.lite.common.spire.client.errorhandler.DefaultErrorNodeErrorHandler;
import uk.gov.bis.lite.common.spire.client.errorhandler.ErrorHandler;
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
  private final ErrorHandler errorHandler;
  private final boolean failOnSoapFault;

  /**
   * SpireClient
   *
   * @param parser          a client specific parser implements SpireParser interface {@link SpireParser}
   * @param clientConfig    spire connection details
   * @param requestConfig   configuration relating to specific Client soap endpoint
   * @param errorHandler    custom error node handling
   * @param failOnSoapFault direct client to check for SoapFaults or not
   */
  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig,
                     ErrorHandler errorHandler, boolean failOnSoapFault) {
    this.parser = parser;
    this.username = clientConfig.getUsername();
    this.password = clientConfig.getPassword();
    this.url = clientConfig.getUrl();
    this.namespace = requestConfig.getNamespace();
    this.requestChildName = requestConfig.getRequestChildName();
    this.useSpirePrefix = requestConfig.isUseSpirePrefix();
    this.errorHandler = errorHandler;
    this.failOnSoapFault = failOnSoapFault;
  }

  /**
   * SpireClient
   *
   * Creates SpireClient setting failOnSoapFault to true
   *
   * @param parser          a client specific parser implements SpireParser interface {@link SpireParser}
   * @param clientConfig    spire connection details
   * @param requestConfig   configuration relating to specific Client soap endpoint
   * @param errorHandler    custom error node handling
   */
  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig,
                     ErrorHandler errorHandler) {
    this(parser, clientConfig, requestConfig, errorHandler, true);
  }

  /**
   * SpireClient
   *
   * Creates SpireClient with DefaultErrorNodeErrorHandler and sets failOnSoapFault to true
   *
   * @param parser          a client specific parser implements SpireParser interface {@link SpireParser}
   * @param clientConfig    spire connection details
   * @param requestConfig   configuration relating to specific Client soap endpoint
   */
  public SpireClient(SpireParser<T> parser, SpireClientConfig clientConfig, SpireRequestConfig requestConfig) {
    this(parser, clientConfig, requestConfig, new DefaultErrorNodeErrorHandler(), true);
  }

  /**
   * Make a client call to Spire
   *
   * @param request request created by SpireClient
   * @return generic type parameter
   */
  public T sendRequest(SpireRequest request) {

    // Get response
    SpireResponse spireResponse = getSpireResponse(request, namespace);

    // Check response message for soap fault if configured
    if (failOnSoapFault) {
      throwSoapFaultSpireException(spireResponse.getMessage());
    }

    // Check response for errors
    errorHandler.checkResponse(spireResponse);

    // Parse and return result
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
    String requestUrl = url + urlSuffix;
    logSoapMessage("request", request.getSoapMessage(), requestUrl);
    SOAPMessage response = doExecuteRequest(request, requestUrl);
    logSoapMessage("response", response, requestUrl);
    if (response == null) {
      throw new SpireClientException("Empty response from SOAP client");
    }
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
      throw new SpireClientException("Error occurred creating the SOAP request for retrieving Customer Information from Spire", e);
    }
  }

  private void addAuthorizationHeader(SOAPMessage message) throws UnsupportedEncodingException {
    MimeHeaders headers = message.getMimeHeaders();
    String authorization = Base64.getEncoder().encodeToString((username + ":" + password).getBytes("utf-8"));
    headers.addHeader("Authorization", "Basic " + authorization);
  }

  private SOAPMessage doExecuteRequest(SpireRequest request, String url) {
    SOAPConnection conn = null;
    try {
      conn = SOAPConnectionFactory.newInstance().createConnection();
      return conn.call(request.getSoapMessage(), url);
    } catch (SOAPException e) {
      throw new SpireClientException("Error occurred establishing connection with SOAP client", e);
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
    try {
      SOAPFault fault = message.getSOAPBody().getFault();
      if (fault != null) {
        String faultInfo = fault.getFaultString() != null ? fault.getFaultString() : "NULL";
        throw new SpireClientException("soap:Fault: [" + faultInfo + "]");
      }
    } catch (SOAPException e) {
      LOGGER.warn("Exception: " + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Logs a serialised SOAP Message, {@code message} is only serialised if the log level is INFO log level is enabled
   * @param type the type of SOAP mesage
   * @param message the message
   * @param url the originating url of {@code message}
   */
  private void logSoapMessage(String type, SOAPMessage message, String url) {
    if (LOGGER.isInfoEnabled()) {
      try {
        String serialisedMessage = "";
        if (message != null) {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          message.writeTo(out);
          serialisedMessage = out.toString();
        }
        LOGGER.info("SOAP " + type + " - url: " + url + ", message:\n" + serialisedMessage);
      } catch (IOException | SOAPException e) {
        LOGGER.error("error", e);
      }
    }
  }

}
