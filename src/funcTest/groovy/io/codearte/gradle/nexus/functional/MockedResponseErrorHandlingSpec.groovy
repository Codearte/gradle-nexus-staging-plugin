package io.codearte.gradle.nexus.functional

import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovy.transform.NotYetImplemented
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.NexusHttpResponseException
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.RepositoryCloser
import org.junit.Rule
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.containing
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.post
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

class MockedResponseErrorHandlingSpec extends Specification {

    private static final String TEST_MOCKED_USERNAME = ''
    private static final String TEST_MOCKED_PASSWORD = ''
    private static final String TEST_MOCKED_STAGING_PROFILE_ID = "5027d084a01a3a"
    private static final String TEST_MOCKED_NOT_EXISTING_REPOSITORY_ID = "xxx"
    private static final String TEST_MOCKED_REPOSITORY_DESCRIPTION = "Mocked repository description"
    private static final String TEST_MOCKED_SERVER_ERROR_JSON_RESPONSE = """
                        {
                            "errors": [
                                {
                                    "id": "*",
                                    "msg": "Unhandled: Missing staging repository: $TEST_MOCKED_NOT_EXISTING_REPOSITORY_ID"
                                }
                            ]
                        }
                        """.stripIndent()

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(MockedFunctionalSpec.WIREMOCK_RANDOM_PORT)

    def "should present response body on 500 server error"() {
        given:
            SimplifiedHttpJsonRestClient client = new SimplifiedHttpJsonRestClient(new RESTClient(), TEST_MOCKED_USERNAME, TEST_MOCKED_PASSWORD)
            RepositoryCloser closer = new RepositoryCloser(client, getMockedUrl(), TEST_MOCKED_REPOSITORY_DESCRIPTION)
        and:
            stubFor(post(urlEqualTo("/staging/bulk/close"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Accept", containing("application/json"))
                    .willReturn(aResponse()
                    .withStatus(500)
                    .withBody(TEST_MOCKED_SERVER_ERROR_JSON_RESPONSE)
                    .withHeader("Content-Type", "application/json")))
        when:
            closer.closeRepositoryWithIdAndStagingProfileId(TEST_MOCKED_NOT_EXISTING_REPOSITORY_ID, TEST_MOCKED_STAGING_PROFILE_ID)
        then:
            NexusHttpResponseException e = thrown()
            e.statusCode == 500
            e.message.contains("Missing staging repository: $TEST_MOCKED_NOT_EXISTING_REPOSITORY_ID")
            e.cause instanceof HttpResponseException
    }

    private String getMockedUrl() {
        return "http://localhost:${wireMockRule.port()}/"
    }

    @NotYetImplemented
    def "should present response body on 400 or 500 errors with plain text response"() {}
}
