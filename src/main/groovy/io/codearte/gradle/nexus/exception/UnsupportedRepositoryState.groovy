package io.codearte.gradle.nexus.exception

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.infra.NexusStagingException
import io.codearte.gradle.nexus.logic.RepositoryState

@CompileStatic
class UnsupportedRepositoryState extends NexusStagingException {

    final String unsupportedState

    UnsupportedRepositoryState(String unsupportedState) {
        super("Unsupported repository state '${unsupportedState}'. Supported values: ${RepositoryState.values()}")
        this.unsupportedState = unsupportedState
    }
}
