package uk.gov.bis.lite.common.spire.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;
import uk.gov.bis.lite.common.spire.client.parser.SpireParser;

import java.util.List;

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
  public void testSimpleValidRequestWithoutUsingSpirePrefix() {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);

    // Verify request
    List<LoggedRequest> requests = findAll(anyRequestedFor(anyUrl()));
    assertThat(requests).hasSize(1);
    Request loggedRequest = requests.get(0);
    assertThat(loggedRequest.getHeader("Authorization")).isEqualTo("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
    assertThat(loggedRequest.getHeader("Content-Type")).isEqualTo("text/xml; charset=UTF-8");
    assertThat(loggedRequest.getAbsoluteUrl()).isEqualTo("http://localhost:8089/NAMESPACE");
    assertThat(loggedRequest.getMethod()).isEqualTo(RequestMethod.POST);
    assertThat(requests.get(0).getBodyAsString()).isEqualTo(fixture("simpleRequest.xml"));

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
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    SpireRequest request = spireClient.createRequest();
    String response = spireClient.sendRequest(request);

    // Verify request
    List<LoggedRequest> requests = findAll(anyRequestedFor(anyUrl()));
    assertThat(requests).hasSize(1);
    Request loggedRequest = requests.get(0);
    assertThat(loggedRequest.getHeader("Authorization")).isEqualTo("Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
    assertThat(loggedRequest.getHeader("Content-Type")).isEqualTo("text/xml; charset=UTF-8");
    assertThat(loggedRequest.getAbsoluteUrl()).isEqualTo("http://localhost:8089/NAMESPACE");
    assertThat(loggedRequest.getMethod()).isEqualTo(RequestMethod.POST);
    assertThat(requests.get(0).getBodyAsString()).isEqualTo(fixture("simpleRequestWithPrefix.xml"));

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
}
