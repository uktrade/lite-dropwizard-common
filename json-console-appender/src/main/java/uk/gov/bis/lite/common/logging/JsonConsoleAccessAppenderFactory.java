package uk.gov.bis.lite.common.logging;

import ch.qos.logback.classic.LoggerContext;
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
import net.logstash.logback.encoder.LogstashAccessEncoder;
import net.logstash.logback.fieldnames.LogstashAccessFieldNames;

/**
 * Logback console log appender factory that will generate JSON for regular access log events.
 * @param <E>
 */
@JsonTypeName("json-console-access")
public class JsonConsoleAccessAppenderFactory<E extends DeferredProcessingAware> extends AbstractAppenderFactory<E> {

  private String appenderName = "json-console-access";
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
    LogstashAccessEncoder encoder = new LogstashAccessEncoder();
    encoder.setContext(context);
    // To log request headers you have to explicitly set the field name for it
    //  https://github.com/logstash/logstash-logback-encoder#header-fields
    LogstashAccessFieldNames fieldNames = encoder.getFieldNames();
    fieldNames.setFieldsRequestHeaders("@fields.request_headers");
    encoder.setFieldNames(fieldNames);
    // Lower case header names makes matching a bit easier, so later analysis might be a bit more reliable
    encoder.setLowerCaseHeaderNames(true);
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
