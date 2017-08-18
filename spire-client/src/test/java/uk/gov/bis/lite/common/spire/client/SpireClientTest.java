package uk.gov.bis.lite.common.spire.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SpireClientTest {

  private SpireClient<String> client;

  @ClassRule
  public static WireMockRule wireMockRule = new WireMockRule(8089);

  public SpireClientTest() {
    SpireParser<String> parser = new ReferenceParser("ELEMENT");
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", "http://localhost:8089/");
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    client = new SpireClient<>(parser, clientConfig, requestConfig);
  }

  @BeforeClass
  public static void before() throws Exception {
    wireMockRule.start();
  }

  @After
  public void tearDown() throws Exception {
    wireMockRule.resetAll();
  }

  @AfterClass
  public static void after() throws Exception {
    wireMockRule.stop();
  }

  @Test
  public void testSimpleValidRequestWithoutUsingSpirePrefix() {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .withHeader("Authorization", equalTo("Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
        .withHeader("Content-Type", equalTo("text/xml; charset=UTF-8"))
        .withRequestBody(equalTo(fixture("simpleRequest.xml")))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);

    // Verify response
    assertThat(response).isEqualTo("TEXT");
  }

  @Test
  public void testSimpleValidRequestUsingSpirePrefix() {
    SpireClient<String> spireClient = new SpireClient<>(
        new ReferenceParser("ELEMENT"),
        new SpireClientConfig("username", "password", "http://localhost:8089/"),
        new SpireRequestConfig("NAMESPACE", "CHILD", true));

    stubFor(post(urlEqualTo("/NAMESPACE"))
        .withHeader("Authorization", equalTo("Basic dXNlcm5hbWU6cGFzc3dvcmQ="))
        .withHeader("Content-Type", equalTo("text/xml; charset=UTF-8"))
        .withRequestBody(equalTo(fixture("simpleRequestWithPrefix.xml")))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = spireClient.createRequest();
    String response = spireClient.sendRequest(request);

    // Verify response
    assertThat(response).isEqualTo("TEXT");
  }

  @Test
  public void testEmptyResponseShouldThrowKnownException() {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(500))
    );
    SpireRequest request = client.createRequest();
    assertThatThrownBy(() -> client.sendRequest(request))
        .isExactlyInstanceOf(SpireClientException.class)
        .hasMessageEndingWith("Empty response from SOAP client");
  }

  @Test
  public void testUrlMissingTrailingSlash() {
    SpireParser<String> parser = new ReferenceParser("ELEMENT");
    // Note, url missing trailing slash
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", "http://localhost:8089/some-path");
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    SpireClient<String> client = new SpireClient<>(parser, clientConfig, requestConfig);
    stubFor(post(urlEqualTo("/some-path/NAMESPACE"))
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
  public void testReadTimeoutShouldThrowKnownException() {
    SpireParser<String> parser = new ReferenceParser("ELEMENT");
    int connectTimeoutMillis = 200;
    int readTimeoutMillis = 1000;
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", "http://localhost:8089/some-path", connectTimeoutMillis, readTimeoutMillis);
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    SpireClient<String> client = new SpireClient<>(parser, clientConfig, requestConfig);
    stubFor(post(urlEqualTo("/some-path/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(70000)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = client.createRequest();

    Instant before = Instant.now();
    assertThatThrownBy(() -> client.sendRequest(request)).hasCauseInstanceOf(SOAPExceptionImpl.class);
    Instant after = Instant.now();

    // 10000ms is less that the SpireClientConfig default of 60000ms
    assertThat(ChronoUnit.MILLIS.between(before, after)).isBetween(1000L, 10000L);
  }
}
