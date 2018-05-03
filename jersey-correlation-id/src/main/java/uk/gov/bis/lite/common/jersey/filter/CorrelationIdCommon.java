package uk.gov.bis.lite.common.jersey.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

public class CorrelationIdCommon {
  private static final Logger LOGGER = LoggerFactory.getLogger(CorrelationIdCommon.class);

  /**
   * HTTP Header that should contain the Correlation ID when receiving or creating a request
   */
  protected static final String HTTP_HEADER_NAME = "X-CorrelationId";

  /**
   * Key to store the Correlation ID against in MDC for logging, use <code>{@code %mdc{corrID} }</code> in a log pattern to log the ID on all messages
   */
  protected static final String MDC_KEY = "corrID";

  /**
   * Get the Correlation ID for this request, or create one if one couldn't be found.
   *
   * @return Correlation ID
   */
  public static String get() {
    // Note that the Correlation ID storage is using MDC (http://logback.qos.ch/manual/mdc.html) which is backed by a
    //   ThreadLocal and considerations should be made when creating async code
    String correlationId = MDC.get(MDC_KEY);
    if (correlationId == null || correlationId.isEmpty()) {
      correlationId = createCorrelationId();
    }
    return correlationId;
  }

  /**
   * Create a new Correlation ID and put it in MDC
   */
  protected static String createCorrelationId() {
    String newCorrelationId = UUID.randomUUID().toString();
    MDC.put(MDC_KEY, newCorrelationId);
    LOGGER.debug("Correlation ID not found in headers, created new correlation id: {}", newCorrelationId);
    return newCorrelationId;
  }
}
