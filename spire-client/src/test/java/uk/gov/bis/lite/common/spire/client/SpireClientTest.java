package uk.gov.bis.lite.common.spire.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

import javax.xml.soap.SOAPException;

public class SpireClientTest {

  private SpireClient<String> client;

  @ClassRule
  public static WireMockClassRule wireMockClassRule = new WireMockClassRule(options().dynamicPort());

  public SpireClientTest() {
    SpireParser<String> parser = new ReferenceParser("ELEMENT");
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", getSpireBaseUrl());
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    client = new SpireClient<>(parser, clientConfig, requestConfig);
  }

  @BeforeClass
  public static void before() throws Exception {
    configureFor(wireMockClassRule.port());
  }

  @After
  public void tearDown() throws Exception {
    wireMockClassRule.resetAll();
  }

  private String getSpireBaseUrl() {
    return "http://localhost:" + wireMockClassRule.port() + "/";
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
        new SpireClientConfig("username", "password", getSpireBaseUrl()),
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
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", getSpireBaseUrl() + "some-path");
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
    SpireClientConfig clientConfig = new SpireClientConfig("username", "password", getSpireBaseUrl() + "some-path", connectTimeoutMillis, readTimeoutMillis);
    SpireRequestConfig requestConfig = new SpireRequestConfig("NAMESPACE", "CHILD", false);
    SpireClient<String> client = new SpireClient<>(parser, clientConfig, requestConfig);
    stubFor(post(urlEqualTo("/some-path/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(200)
            .withFixedDelay(10000)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = client.createRequest();
      assertThatThrownBy(() -> client.sendRequest(request)).hasCauseInstanceOf(SOAPException.class);
  }
}
