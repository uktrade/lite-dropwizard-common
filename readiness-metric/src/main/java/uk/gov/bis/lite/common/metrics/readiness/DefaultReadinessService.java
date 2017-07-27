package uk.gov.bis.lite.common.metrics.readiness;

/**
 * A default implementation of the ReadinessService which will always return true, see {@link #isReady()}.
 */
public class DefaultReadinessService implements ReadinessService {
  @Override
  public boolean isReady() {
    return true;
  }
}
