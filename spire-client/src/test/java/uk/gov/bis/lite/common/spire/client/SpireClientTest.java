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

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.bis.lite.common.spire.client.exception.SpireClientException;
import uk.gov.bis.lite.common.spire.client.parser.ReferenceParser;

public class SpireClientTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(options().dynamicPort());

  @Before
  public void before() throws Exception {
    configureFor(wireMockRule.port());
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

    String spireUrl = "http://localhost:" + wireMockRule.port() + "/";
    SpireClient<String> client =  new SpireClient<>(
        new ReferenceParser("ELEMENT"),
        new SpireClientConfig("username", "password", spireUrl),
        new SpireRequestConfig("NAMESPACE", "CHILD", false));

    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);

    // Verify response
    assertThat(response).isEqualTo("TEXT");
  }

  @Test
  public void testSimpleValidRequestUsingSpirePrefix() {
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

    String spireUrl = "http://localhost:" + wireMockRule.port() + "/";
    SpireClient<String> client =  new SpireClient<>(
        new ReferenceParser("ELEMENT"),
        new SpireClientConfig("username", "password", spireUrl),
        new SpireRequestConfig("NAMESPACE", "CHILD", true));

    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);

    // Verify response
    assertThat(response).isEqualTo("TEXT");
  }

  @Test
  public void testEmptyResponseShouldThrowKnownException() {
    stubFor(post(urlEqualTo("/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(500))
    );

    String spireUrl = "http://localhost:" + wireMockRule.port() + "/";
    SpireClient<String> client =  new SpireClient<>(
        new ReferenceParser("ELEMENT"),
        new SpireClientConfig("username", "password", spireUrl),
        new SpireRequestConfig("NAMESPACE", "CHILD", false));

    SpireRequest request = client.createRequest();
    assertThatThrownBy(() -> client.sendRequest(request))
        .isExactlyInstanceOf(SpireClientException.class)
        .hasMessageEndingWith("Empty response from SOAP client");
  }

  @Test
  public void testUrlMissingTrailingSlash() {
    stubFor(post(urlEqualTo("/some-path/NAMESPACE"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/soap+xml; charset=utf-8")
            .withBodyFile("simple.xml")
        )
    );

    // Note, url missing trailing slash
    String spireUrl = "http://localhost:" + wireMockRule.port() + "/some-path";
    SpireClient<String> client =  new SpireClient<>(
        new ReferenceParser("ELEMENT"),
        new SpireClientConfig("username", "password", spireUrl),
        new SpireRequestConfig("NAMESPACE", "CHILD", false));

    SpireRequest request = client.createRequest();
    String response = client.sendRequest(request);
    assertThat(response).isEqualTo("TEXT");
  }
}
