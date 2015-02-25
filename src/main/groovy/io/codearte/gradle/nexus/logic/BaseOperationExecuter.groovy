package io.codearte.gradle.nexus.logic

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

//@TupleConstructor //TODO: Hmm?
abstract class BaseOperationExecuter {

    protected final SimplifiedHttpJsonRestClient client
    protected final String nexusUrl

    BaseOperationExecuter(SimplifiedHttpJsonRestClient client, String nexusUrl) {
        this.client = client
        this.nexusUrl = nexusUrl
    }
}
