package uk.gov.bis.lite.common.metrics.readiness;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * An interface for describing the readiness of a service.
 */
public interface ReadinessService {

  /**
   * Is the service ready to receive requests, are all components loaded with data?
   * Defaults to {@code true}
   * @return readiness of the service
   */
  boolean isReady();

  /**
   * <p>
   *   The JSON description of the services readiness. Will return the serialisation of {@link #isReady()} by default.
   *   e.g. {@code {"ready": true}} or {@code {"ready": false}}
   * </p>
   * <p>
   *   Consider adding additional information to this JSON object, such as reasons for a negative readiness. e.g.
   *   {@code {"ready": false, "message":"waiting for initial cache load"}}
   * </p>
   * @return a JSON description of the services readiness.
   */
  default JsonNode readinessJson() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.getNodeFactory().objectNode();
    node.put("ready", isReady());
    return node;
  }
}

