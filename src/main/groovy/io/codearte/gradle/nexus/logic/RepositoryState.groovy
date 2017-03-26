package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.exception.UnsupportedRepositoryState

@CompileStatic
enum RepositoryState {
    //TODO: PROMOTED and RELEASED is confusing
    OPEN,
    CLOSED,
    /** @deprecated */
    PROMOTED,
    RELEASED,
    NOT_FOUND

    @Override
    String toString() {
        return name().toLowerCase()
    }

    static RepositoryState parseString(String stateAsString) {
        try {
            return valueOf(stateAsString?.toUpperCase())
        } catch (IllegalArgumentException | NullPointerException ignored) {
            throw new UnsupportedRepositoryState(stateAsString)
        }
    }
}
