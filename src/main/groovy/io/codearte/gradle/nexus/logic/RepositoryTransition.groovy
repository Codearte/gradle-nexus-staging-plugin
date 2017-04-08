package io.codearte.gradle.nexus.logic

interface RepositoryTransition {

    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId)

    List<RepositoryState> desiredAfterTransitionRepositoryState()
}
