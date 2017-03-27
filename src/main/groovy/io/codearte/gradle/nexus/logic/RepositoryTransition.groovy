package io.codearte.gradle.nexus.logic

interface RepositoryTransition {

    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId)

    //TODO: Should return list if auto drop is implemented - #37
    RepositoryState desiredAfterTransitionRepositoryState()
}
