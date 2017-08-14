package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

//@TupleConstructor //TODO: Does not want to work
@CompileStatic
abstract class BaseOperationExecutor {

    protected final SimplifiedHttpJsonRestClient client
    protected final String nexusUrl
    protected final String repositoryDescription

    BaseOperationExecutor(SimplifiedHttpJsonRestClient client, String nexusUrl, String repositoryDescription = 'Automatically released/promoted with gradle-nexus-staging-plugin!') {
        this.client = client
        this.nexusUrl = removeTrailingSlashIfAvailable(nexusUrl)
        this.repositoryDescription = repositoryDescription
    }

    private String removeTrailingSlashIfAvailable(String nexusUrl) {
        nexusUrl.endsWith("/") ? nexusUrl[0..-2] : nexusUrl
    }
}
