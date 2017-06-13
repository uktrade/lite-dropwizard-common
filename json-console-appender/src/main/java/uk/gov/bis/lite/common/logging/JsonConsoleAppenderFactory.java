package uk.gov.bis.lite.common.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.AbstractAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;
import net.logstash.logback.encoder.LogstashEncoder;

/**
 * Logback console log appender factory that will generate JSON for regular logback log events.
 * @param <E>
 */
@JsonTypeName("json-console")
public class JsonConsoleAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {

  private String appenderName = "json-console";
  private boolean includeContextName = false;

  @JsonProperty
  public String getName() {
    return this.appenderName;
  }

  @JsonProperty
  public void setName(String name) {
    this.appenderName = name;
  }

  @JsonProperty
  public boolean getIncludeContextName() {
    return this.includeContextName;
  }

  @JsonProperty
  public void setIncludeContextName(boolean includeContextName) {
    this.includeContextName = includeContextName;
  }

  @Override
  public Appender<E> build(LoggerContext context, String applicationName, LayoutFactory<E> layoutFactory,
                           LevelFilterFactory<E> levelFilterFactory, AsyncAppenderFactory<E> asyncAppenderFactory) {
    LogstashEncoder encoder = new LogstashEncoder();
    encoder.setContext(context);
    // Make sure we include MDC, which is where our Correlation ID would be stored
    encoder.setIncludeMdc(true);
    // Set a throwable converter so that stack traces come write out as JSON in full rather than just the message
    encoder.setThrowableConverter(new ThrowableProxyConverter());
    encoder.start();

    ConsoleAppender<E> appender = new ConsoleAppender<>();
    appender.setName(appenderName);
    appender.setContext(context);
    appender.setEncoder((Encoder<E>) encoder);

    appender.addFilter(levelFilterFactory.build(threshold));
    getFilterFactories().forEach(f -> appender.addFilter(f.build()));

    appender.start();

    return wrapAsync(appender, asyncAppenderFactory);
  }
}
