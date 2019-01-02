package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryReleaser
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.RetryingRepositoryTransitioner
import org.gradle.api.tasks.TaskAction

@CompileStatic
@InheritConstructors(constructorAnnotations = true)
class ReleaseRepositoryTask extends BaseStagingTask {

    @TaskAction
    void releaseRepository() {
        //TODO: Remove once stagingProfileId migrated to plugin extension
        tryToTakeStagingProfileIdFromCloseRepositoryTask()

        String stagingProfileId = getConfiguredStagingProfileIdOrFindAndCacheOne(createProfileFetcherWithGivenClient(createClient()))
        String repositoryId = getConfiguredRepositoryIdForStagingProfileOrFindAndCacheOneInGivenState(stagingProfileId, RepositoryState.CLOSED)

        releaseRepositoryByIdAndProfileIdWithRetrying(repositoryId, stagingProfileId)
    }

    private void tryToTakeStagingProfileIdFromCloseRepositoryTask() {
        if (getStagingProfileId() != null) {
            return
        }
        String stagingProfileIdFromCloseRepositoryTask = getCloseRepositoryTask().stagingProfileId

        if (stagingProfileIdFromCloseRepositoryTask != null) {
            logger.debug("Reusing staging profile id from closeRepository task: $stagingProfileIdFromCloseRepositoryTask")
            setStagingProfileId(stagingProfileIdFromCloseRepositoryTask)
        }
    }

    private CloseRepositoryTask getCloseRepositoryTask() {
        return project.tasks.withType(CloseRepositoryTask)[0]
    }

    private void releaseRepositoryByIdAndProfileIdWithRetrying(String repositoryId, String stagingProfileId) {
        RepositoryReleaser repositoryReleaser = createRepositoryReleaserWithGivenClient(createClient())
        RepositoryStateFetcher repositoryStateFetcher = createRepositoryStateFetcherWithGivenClient(createClient())
        OperationRetrier<RepositoryState> retrier = createOperationRetrier()
        RetryingRepositoryTransitioner retryingReleaser = new RetryingRepositoryTransitioner(repositoryReleaser, repositoryStateFetcher, retrier)

        retryingReleaser.performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
        logger.info("Repository '$repositoryId' has been effectively released")
    }
}
