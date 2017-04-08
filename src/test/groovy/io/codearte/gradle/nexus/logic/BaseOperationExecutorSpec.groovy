package io.codearte.gradle.nexus.logic

import spock.lang.Specification

abstract class BaseOperationExecutorSpec extends Specification {

    protected static final String TEST_STAGING_PROFILE_ID = "5027d084a01a3a"
    protected static final String TEST_REPOSITORY_ID = "iocodearte-1011"
    protected static final String MOCK_SERVER_HOST = "https://mock.server"

    protected static String pathForGivenBulkOperation(String operationName) {
        return "${MOCK_SERVER_HOST}/staging/bulk/$operationName"
    }

}
