package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient

/**
 * Specialized REST client to communicate with Nexus.
 *
 * Note: The same as RESTClient from HTTP Builder this class is not thread-safe.
 */
@CompileStatic
@Slf4j
class SimplifiedHttpJsonRestClient {

    private final RESTClient restClient
    private final String username
    private final String password

    SimplifiedHttpJsonRestClient(RESTClient restClient, String username, String password) {
        this.restClient = restClient
        this.username = username
        this.password = password
//        params.requestContentType = ContentType.JSON  //Does not set Content-Type header as required by WireMock
        restClient.headers["Content-Type"] = "application/json"
    }

    Map get(String uri) {
        setUriAndAuthentication(uri)
        Map params = createAndInitializeCallParametersMap()
        HttpResponseDecorator response = (HttpResponseDecorator)restClient.get(params)
        log.debug("GET response data: ${response.data}")
        return (Map)response.data
    }

    private Map createAndInitializeCallParametersMap() {    //New for every call - it is cleared up after call by RESTClient
        return [contentType: ContentType.JSON]
    }

    private void setUriAndAuthentication(String uri) {
        restClient.uri = uri
        if (username != null) {
            restClient.auth.basic(username, password)   //has to be after URI is set
        }
    }

    void post(String uri, Map content) {
        setUriAndAuthentication(uri)
        Map params = createAndInitializeCallParametersMap()
        params.body = content
        try {
            log.debug("POST request content: $content")
            HttpResponseDecorator response = (HttpResponseDecorator) restClient.post(params)
            log.debug("POST response status ${response.status}, data: ${response.data}")
        } catch (HttpResponseException e) {
            //Enhance rethrown exception to contain also response body - #5
            //TODO: Still better handle response content type on 404 and 50x - server returns 'text/plain', but RESTClient from Groovy Builder tries to parse it as JSON
            HttpResponseDecorator resp = e.getResponse();
            String message = "${resp.statusLine.statusCode}: ${resp.statusLine.reasonPhrase}, body: ${resp.data}"
            log.warn("POST request failed. ${message}")
            throw new NexusHttpResponseException(e.getStatusCode(), message, e)
        }
    }
}
