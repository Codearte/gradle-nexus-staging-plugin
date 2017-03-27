package io.codearte.gradle.nexus.exception

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.infra.NexusStagingException

@CompileStatic
class RepositoryInTransitionException extends NexusStagingException {

    final String repositoryId
    final String state

    RepositoryInTransitionException(String repositoryId, String state) {
        super("Repository '$repositoryId' (in '$state' state) is in transition. Check again later.")
        this.repositoryId = repositoryId
        this.state = state
    }
}
