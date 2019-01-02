package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.RetryingRepositoryTransitioner
import org.gradle.api.tasks.TaskAction

@CompileStatic
@InheritConstructors(constructorAnnotations = true)
class CloseRepositoryTask extends BaseStagingTask {

    @SuppressWarnings("unused")
    @TaskAction
    void closeRepository() {
        String stagingProfileId = getConfiguredStagingProfileIdOrFindAndCacheOne(createProfileFetcherWithGivenClient(createClient()))
        String repositoryId = getConfiguredRepositoryIdForStagingProfileOrFindAndCacheOneInGivenState(stagingProfileId, RepositoryState.OPEN)

        closeRepositoryByIdAndProfileIdWithRetrying(repositoryId, stagingProfileId)
    }

    private void closeRepositoryByIdAndProfileIdWithRetrying(String repositoryId, String stagingProfileId) {
        RepositoryCloser repositoryCloser = createRepositoryCloserWithGivenClient(createClient())
        RepositoryStateFetcher repositoryStateFetcher = createRepositoryStateFetcherWithGivenClient(createClient())
        OperationRetrier<RepositoryState> retrier = createOperationRetrier()
        RetryingRepositoryTransitioner retryingCloser = new RetryingRepositoryTransitioner(repositoryCloser, repositoryStateFetcher, retrier)

        retryingCloser.performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
        logger.info("Repository '$repositoryId' has been effectively closed")
    }
}
