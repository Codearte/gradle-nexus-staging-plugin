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

    private enum RequestType {
        GET, POST
    }

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
        return (Map) sendRequestHandlingErrors(uri, null, restClient.&get, RequestType.GET).data
    }

    Map post(String uri, Map content) {
        return (Map) sendRequestHandlingErrors(uri, content, restClient.&post, RequestType.POST).data
    }

    private HttpResponseDecorator sendRequestHandlingErrors(String uri, Map content, Closure<Object> clientMethodHandler, RequestType requestTypeName) {
        try {
            return prepareAndSendRequest(uri, content, clientMethodHandler, requestTypeName)
        } catch (HttpResponseException e) {
            HttpResponseDecorator resp = e.getResponse();
            String message = "${resp.statusLine.statusCode}: ${resp.statusLine.reasonPhrase}, body: ${resp.data ?: '<empty>'}"
            //TODO: Suppress error message on 404 if waiting for drop?
            log.warn("$requestTypeName request failed. ${message}")
            throw new NexusHttpResponseException(e.getStatusCode(), message, e)
        }
    }

    private HttpResponseDecorator prepareAndSendRequest(String uri, Map content, Closure<Object> clientMethodHandler, RequestType requestType) {
        setUriAndAuthentication(uri)
        Map params = createAndInitializeCallParametersMap()
        if (content != null) {
            params.body = content
            log.debug("$requestType request content: $content")
        }
        log.debug("$requestType request URL: $uri")
        return (HttpResponseDecorator) clientMethodHandler(params)
    }

    private void setUriAndAuthentication(String uri) {
        restClient.uri = uri
        if (username != null) {
            restClient.auth.basic(username, password)   //has to be after URI is set
        }
    }

    private Map createAndInitializeCallParametersMap() {    //New for every call - it is cleared up after call by RESTClient
        return [contentType: ContentType.JSON]
    }
}
