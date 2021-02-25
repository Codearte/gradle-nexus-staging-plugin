package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic

/**
 * Custom exception to propagate server errors.
 *
 * Created as OkHttp does not throw exceptions for HTTP errors (e.g. Server Error)
 * which in many cases is crucial to determine the resons why error was returned.
 *
 * It may be made redundant once migrated to other HTTP library.
 */
@CompileStatic
class NexusHttpResponseException extends NexusStagingException {

    final int statusCode

    NexusHttpResponseException(int statusCode, String message) {
        super(message)
        this.statusCode = statusCode
    }
}
