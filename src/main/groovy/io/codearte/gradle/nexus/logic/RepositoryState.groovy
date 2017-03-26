package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic

@CompileStatic
enum RepositoryState {
    //TODO: PROMOTED and RELEASED is confusing
    OPEN,
    CLOSED,
    /** @deprecated */
    PROMOTED,
    RELEASED,
    NOT_FOUND

    String value() {
        return name().toLowerCase()
    }
}
