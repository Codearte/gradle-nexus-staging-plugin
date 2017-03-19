package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic

/**
 * Custom exception to propagate server errors.
 *
 * Created as groovyx.net.http.HttpResponseException contains in a message only a reason phrase (e.g. Server Error) without response body
 * which in many cases is crucial to determine the resons why error was returned.
 *
 * It may be made redundant once migrated to other HTTP library.
 */
@CompileStatic
class NexusHttpResponseException extends NexusStagingException {

    final int statusCode

    NexusHttpResponseException(int statusCode, String message, Throwable cause) {
        super(message, cause)
        this.statusCode = statusCode
    }
}
