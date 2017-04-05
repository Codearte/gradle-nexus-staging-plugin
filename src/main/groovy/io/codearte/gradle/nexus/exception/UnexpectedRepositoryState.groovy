package io.codearte.gradle.nexus.exception

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.infra.NexusStagingException
import io.codearte.gradle.nexus.logic.RepositoryState

@CompileStatic
class UnexpectedRepositoryState extends NexusStagingException {

    final String repoId
    final String actualState
    final String expectedStates

    UnexpectedRepositoryState(String repoId, RepositoryState actualState, List<RepositoryState> expectedStates) {
        super("Wrong '$repoId' repository state '$actualState' after transition (expected $expectedStates). " +
            "Possible staging rules violation. Check repository status using Nexus UI.")
        this.expectedStates = expectedStates
        this.actualState = actualState
        this.repoId = repoId
    }
}
