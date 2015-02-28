package io.codearte.gradle.nexus.infra

import groovy.transform.CompileStatic

@CompileStatic
class WrongNumberOfRepositories extends NexusStagingException {

    final int numberOfRepositories
    final String state

    WrongNumberOfRepositories(int numberOfRepositories, String state) {
        super("Wrong number of received repositories in state '$state'. Expected 1, received $numberOfRepositories")
        this.numberOfRepositories = numberOfRepositories
        this.state = state
    }
}
