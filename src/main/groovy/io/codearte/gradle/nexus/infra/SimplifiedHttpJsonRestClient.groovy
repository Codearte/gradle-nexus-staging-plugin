package io.codearte.gradle.nexus.infra

import groovy.json.JsonBuilder
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
    private final Map params

    SimplifiedHttpJsonRestClient(RESTClient restClient, String username, String password) {
        this.restClient = restClient
        this.username = username
        this.password = password
        params = [:]
        params.contentType = ContentType.JSON
    }

    Map get(String uri) {
        setUriAndAuthentication(uri)
        HttpResponseDecorator response = (HttpResponseDecorator)restClient.get(params)
        log.debug("GET response data: ${response.data}")
        log.debug("GET response data as JSON: ${new JsonBuilder(response.data).toString()}")    //TODO: Remove
        return (Map)response.data
    }

    private void setUriAndAuthentication(String uri) {
        restClient.uri = uri
        restClient.auth.basic(username, password)   //has to be after URI is set
    }

    void post(String uri, Map content) {
        setUriAndAuthentication(uri)
        params.body = content
        //TODO: Add better error handling (e.g. display error message received from server, not only 500 + not fail on 404 in 'text/html')
        HttpResponseDecorator response = (HttpResponseDecorator)restClient.post(params)
        log.warn("POST response data: ${response.data}")
        log.debug("POST response data as JSON: ${new JsonBuilder(response.data).toString()}")    //TODO: Remove
    }
}
