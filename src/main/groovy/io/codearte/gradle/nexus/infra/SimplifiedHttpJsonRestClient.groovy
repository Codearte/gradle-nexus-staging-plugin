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
    private final Map params

    SimplifiedHttpJsonRestClient(RESTClient restClient, String username, String password) {
        this.restClient = restClient
        this.username = username
        this.password = password
        params = [:]
        params.contentType = ContentType.JSON
    }

    Map get(String uri) {
        restClient.uri = uri
        restClient.auth.basic(username, password)   //has to be after URI is set
        HttpResponseDecorator response = (HttpResponseDecorator)restClient.get(params)
        log.debug("GET response data: $response.data")
        return (Map)response.data
    }
}
