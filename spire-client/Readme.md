# SpireClient

Provides utility framework for accessing Spire data.

## How to use

Add the dependency to your project:

```gradle
compile 'uk.gov.bis.lite:spire-client:1.6'
```

Add configuration to your Dropwizard application:

```config
spireClientUserName: username
spireClientPassword: password
spireClientUrl: https://www.spiretest.trade.gov.uk/spireuat/fox/ispire/
```

SpireReferenceClient - create a SpireClient A client requires a client specific
parser and also needs request and spire configuration data:

```java
public class SpireReferenceClient extends SpireClient<String> {
  public SpireReferenceClient(SpireParser<String> parser,
                              SpireClientConfig clientConfig,
                              SpireRequestConfig requestConfig) {
    super(parser, clientConfig, requestConfig);
  }
}
```

To use the SpireReferenceClient provide an instance via GuiceModule
using the framework provided ReferenceParer:

```java
  @Provides
  @Singleton
  SpireReferenceClient provideCreateLiteSar(Environment env, CustomerApplicationConfiguration config) {
    return new SpireReferenceClient(
        new ReferenceParser("SAR_REF"),
        new SpireClientConfig(config.getSpireClientUserName(), config.getSpireClientPassword(), config.getSpireClientUrl()),
        new SpireRequestConfig("SPIRE_CREATE_LITE_SAR", "SAR_DETAILS", false));
  }
```

Use the Client to create a request and get result from sent request:

```java
    SpireRequest request = spireReferenceClient.createRequest();
    request.addChild("VERSION_NO", "1.1");
    request.addChild("WUA_ID", item.getUserId());
    // ...
    String result = spireReferenceClient.sendRequest(request);    
```

For more complex Soap endpoints you create your own parser (as well as Client).
This example is used to extract companies from the COMPANY Soap endpoint:

```java
public class CompanyParser implements SpireParser<List<SpireCompany>> {

  @Override
  public List<SpireCompany> parseResponse(SpireResponse spireResponse) {
    return getCompaniesFromNodes(spireResponse.getElementChildNodesForList("//COMPANIES_LIST"));
  }

  private List<SpireCompany> getCompaniesFromNodes(List<Node> nodes) {
    List<SpireCompany> companies = new ArrayList<>();
    for (Node node : nodes) {
      SpireCompany company = new SpireCompany();
      SpireResponse.getNodeValue(node, "SAR_REF").ifPresent(company::setSarRef);
      SpireResponse.getNodeValue(node, "NAME").ifPresent(company::setName);
      SpireResponse.getNodeValue(node, "SHORT_NAME").ifPresent(company::setShortName);
      SpireResponse.getNodeValue(node, "ORGANISATION_TYPE").ifPresent(v -> company.setSpireOrganisationType(SpireOrganisationType.valueOf(v)));
      SpireResponse.getNodeValue(node, "COMPANY_NUMBER").ifPresent(company::setNumber);
      SpireResponse.getNodeValue(node, "REGISTRATION_STATUS").ifPresent(company::setRegistrationStatus);
      SpireResponse.getNodeValue(node, "REGISTERED_ADDRESS").ifPresent(company::setRegisteredAddress);
      SpireResponse.getNodeValue(node, "APPLICANT_TYPE").ifPresent(company::setApplicantType);
      SpireResponse.getNodeValue(node, "NATURE_OF_BUSINESS").ifPresent(company::setNatureOfBusiness);
      SpireResponse.getNodeValue(node, "COUNTRY_OF_ORIGIN").ifPresent(company::setCountryOfOrigin);

      List<SpireWebsite> webSites = new ArrayList<>();
      List<Node> websiteNodes = SpireResponse.getChildrenOfChildNode(node, "WEBSITE_LIST");
      for (Node websiteNode : websiteNodes) {
        SpireWebsite website = new SpireWebsite();
        SpireResponse.getNodeValue(websiteNode, "WEBSITE_URL").ifPresent(website::setUrl);
        webSites.add(website);
      }
      company.setWebsites(webSites);
      companies.add(company);
    }
    return companies;
  }
}   
```

Finally, ensure that the following `sun` java system properties have reasonable values (defaults are 0), this prevents SOAP 
connections waiting indefinitely on socket `connect` or `read`:

```
sun.rmi.transport.proxy.connectTimeout=20000
sun.net.client.defaultConnectTimeout=20000
sun.net.client.defaultReadTimeout=60000
```

For more examples of how to use the SpireClient see the lite-customer-service project