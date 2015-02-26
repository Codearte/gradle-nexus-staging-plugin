package io.codearte.gradle.nexus.logic

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

//@TupleConstructor //TODO: Hmm?
abstract class BaseOperationExecutor {

    protected final SimplifiedHttpJsonRestClient client
    protected final String nexusUrl

    BaseOperationExecutor(SimplifiedHttpJsonRestClient client, String nexusUrl) {
        this.client = client
        this.nexusUrl = nexusUrl
    }
}
