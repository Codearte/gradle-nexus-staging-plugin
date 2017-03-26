package io.codearte.gradle.nexus.exception

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.infra.NexusStagingException
import io.codearte.gradle.nexus.logic.RepositoryState

@CompileStatic
class UnexpectedRepositoryState extends NexusStagingException {

    final String repoId
    final String actualState
    final String expectedState

    UnexpectedRepositoryState(String repoId, RepositoryState actualState, RepositoryState expectedState) {
        super("Wrong repository '$repoId' state '$actualState' after transition (expected '$expectedState'). " +
            "Possible staging rules violation. Check repository status using Nexus UI.")
        this.expectedState = expectedState
        this.actualState = actualState
        this.repoId = repoId
    }
}
