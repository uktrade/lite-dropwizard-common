package uk.gov.bis.lite.common.jersey.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Jersey container filter for getting and storing inbound Correlation IDs
 */
@PreMatching
@Priority(Integer.MIN_VALUE)
public class ContainerCorrelationIdFilter implements ContainerRequestFilter, ContainerResponseFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerCorrelationIdFilter.class);

  @Override
  public void filter(ContainerRequestContext requestContext) {
    MultivaluedMap<String, String> responseHeaders = requestContext.getHeaders();
    List<String> inboundCorrelationIdHeader = responseHeaders.get(CorrelationIdCommon.HTTP_HEADER_NAME);
    if (inboundCorrelationIdHeader != null && !inboundCorrelationIdHeader.isEmpty()) {
      String inboundCorrelationId = inboundCorrelationIdHeader.get(0);
      if (inboundCorrelationId != null && !inboundCorrelationId.isEmpty()) {
        MDC.put(CorrelationIdCommon.MDC_KEY, inboundCorrelationId);
      }
      else {
        CorrelationIdCommon.createCorrelationId();
      }
    }
    else {
      CorrelationIdCommon.createCorrelationId();
    }
  }

  @Override public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
      /*
       * Intentionally don't remove the correlation id since async responses can potentially run on the same thread
       * So if you complete an async response from the current thread, you will lose the correlation id that was set
       *
       * This is safe because every new request gets a correlation id from the header, there's no way to "leave" a correlation id
       * in a thread since all new threads will get new correlation id
       */
  }
}