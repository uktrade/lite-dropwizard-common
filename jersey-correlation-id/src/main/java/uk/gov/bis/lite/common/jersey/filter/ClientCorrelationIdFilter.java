package uk.gov.bis.lite.common.jersey.filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Jersey client filter to set the Correlation ID on outbound client requests
 */
public class ClientCorrelationIdFilter implements ClientRequestFilter, ClientResponseFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientCorrelationIdFilter.class);

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    MultivaluedMap<String, Object> headers = requestContext.getHeaders();
    if (!headers.containsKey(CorrelationIdCommon.HTTP_HEADER_NAME)) {
      headers.add(CorrelationIdCommon.HTTP_HEADER_NAME, CorrelationIdCommon.get());
    }
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    String outboundCorrelationId = CorrelationIdCommon.get();

    MultivaluedMap<String, String> responseHeaders = responseContext.getHeaders();
    List<String> inboundCorrelationIdHeader = responseHeaders.get(CorrelationIdCommon.HTTP_HEADER_NAME);
    if (inboundCorrelationIdHeader != null && !inboundCorrelationIdHeader.isEmpty()) {
      String inboundCorrelationId = inboundCorrelationIdHeader.get(0);
      if (!outboundCorrelationId.equals(inboundCorrelationId)) {
        LOGGER.warn("Correlation ID not on client response does not match the one sent on the request. Got '{}' expected '{}'",
            inboundCorrelationId, outboundCorrelationId);
      }
    }
  }
}
