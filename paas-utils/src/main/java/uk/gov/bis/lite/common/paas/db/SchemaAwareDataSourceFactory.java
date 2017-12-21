package uk.gov.bis.lite.common.paas.db;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * A DatasourceFactory for parsing Dropwizard config which can read an additional "schema" property and set it as the
 * <tt>currentSchema</tt> query parameter on the JDBC URL. This allows an application to control which PostrgeSQL schema
 * it connects to. <br><br>
 *
 * We can't control the order in which the setters are called. Therefore both set methods must attempt to "fix" the URL.
 * When both schema and URL are available, the stored URL will end up being correct.
 */
public class SchemaAwareDataSourceFactory extends DataSourceFactory {

  private String schema;

  @JsonProperty
  public String getSchema() {
    return schema;
  }

  @JsonProperty
  public void setSchema(String schema) {
    this.schema = schema;
    setUrl(fixUrl(getUrl()));
  }

  @Override
  public void setUrl(String url) {
    super.setUrl(fixUrl(url));
  }

  private String fixUrl(String url) {
    if (url != null && !url.contains("currentSchema=") && StringUtils.isNoneBlank(schema)) {
      if (!url.contains("?")) {
        url += "?";
      } else {
        url += "&";
      }
      url += "currentSchema=" + schema;
    }
    return url;
  }
}
