package uk.gov.bis.lite.common.paas.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.bis.lite.common.paas.db.SchemaAwareDataSourceFactory;

public class SchemaAwareDataSourceFactoryTest {

  @Test
  public void testSetSchemaBeforeUrlWithoutQuery() {

    SchemaAwareDataSourceFactory dsf = new SchemaAwareDataSourceFactory();
    dsf.setSchema("schema_name");
    dsf.setUrl("db.host.com");

    assertThat(dsf.getUrl()).isEqualTo("db.host.com?currentSchema=schema_name");
  }

  @Test
  public void testSetSchemaAfterUrlWithoutQuery() {

    SchemaAwareDataSourceFactory dsf = new SchemaAwareDataSourceFactory();
    dsf.setUrl("db.host.com");
    dsf.setSchema("schema_name");

    assertThat(dsf.getUrl()).isEqualTo("db.host.com?currentSchema=schema_name");
  }

  @Test
  public void testSetSchemaBeforeUrlWithQuery() {

    SchemaAwareDataSourceFactory dsf = new SchemaAwareDataSourceFactory();
    dsf.setSchema("schema_name");
    dsf.setUrl("db.host.com?username=user");

    assertThat(dsf.getUrl()).isEqualTo("db.host.com?username=user&currentSchema=schema_name");
  }

  @Test
  public void testSetSchemaAfterUrlWithQuery() {

    SchemaAwareDataSourceFactory dsf = new SchemaAwareDataSourceFactory();
    dsf.setUrl("db.host.com?username=user");
    dsf.setSchema("schema_name");

    assertThat(dsf.getUrl()).isEqualTo("db.host.com?username=user&currentSchema=schema_name");
  }
}