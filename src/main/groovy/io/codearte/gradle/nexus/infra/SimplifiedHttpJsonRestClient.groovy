package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
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
        log.debug("POST request content: $content")
        //TODO: Add better error handling (e.g. display error message received from server, not only 500 + not fail on 404 in 'text/html')
        HttpResponseDecorator response = (HttpResponseDecorator)restClient.post(params)
        log.warn("POST response data: ${response.data}")
    }
}
