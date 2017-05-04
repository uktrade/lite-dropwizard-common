package uk.gov.bis.lite.common.spire.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

public class SpireClientTest {

  private SpireClient<String> client;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8089);

  public SpireClientTest() {
    SpireParser<String> parser = new ReferenceParser("ELEMENT");
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", "http://localhost:8089/");
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    client = new SpireClient<>(parser, clientConfig, requestConfig);
  }

  @Before
  public void setUp() throws Exception {
    wireMockRule.start();
  }

  @After
  public void tearDown() throws Exception {
    wireMockRule.stop();
  }

  @Test
  public void testSimpleValidRequest() throws Exception {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );
    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);
    assertThat(response).isEqualTo("TEXT");
  }

  @Test
  public void testEmptyResponseShouldThrowKnownException() throws Exception {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(500))
    );
    SpireRequest request = client.createRequest();
    assertThatThrownBy(() -> client.sendRequest(request))
        .isExactlyInstanceOf(SpireClientException.class)
        .hasMessageEndingWith("Empty response from SOAP client");
  }
}
