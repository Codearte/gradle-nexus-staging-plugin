package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.RetryingRepositoryTransitioner
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

@CompileStatic
class CloseRepositoryTask extends BaseStagingTask {

    @Input
    @Optional
    String stagingRepositoryId

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())  //Here or in fetchAndCacheStagingProfileId()?
        RepositoryFetcher repositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryCloser repositoryCloser = createRepositoryCloserWithGivenClient(createClient())
        OperationRetrier<String> legacyRetrier = createOperationRetrier()

        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)

        String repositoryId = findOneOpenRepositoryIdForStagingProfileIdWithRetrying(stagingProfileId, repositoryFetcher, legacyRetrier)

        memorizeRepositoryIdForReusingInInOtherTasks(repositoryId)

        closeRepositoryByIdAndProfileIdWithRetrying(repositoryCloser, repositoryId, stagingProfileId)
    }

    private String findOneOpenRepositoryIdForStagingProfileIdWithRetrying(String stagingProfileId, RepositoryFetcher repositoryFetcher, OperationRetrier<String> legacyRetrier) {
        //TODO: Repository provided by Gradle upload mechanism should be used, but unfortunately it seems to be unsupported by Gradle.
        //      Therefore, check for just one repository in "open" state
        return legacyRetrier.doWithRetry { repositoryFetcher.getOpenRepositoryIdForStagingProfileId(stagingProfileId) }
    }

    private void memorizeRepositoryIdForReusingInInOtherTasks(String repositoryId) {
        stagingRepositoryId = repositoryId
    }

    private void closeRepositoryByIdAndProfileIdWithRetrying(RepositoryCloser repositoryCloser, String repositoryId, String stagingProfileId) {
        RepositoryStateFetcher repositoryStateFetcher = createRepositoryStateFetcherWithGivenClient(createClient())
        OperationRetrier<RepositoryState> retrier = createOperationRetrier()
        RetryingRepositoryTransitioner retryingCloser = new RetryingRepositoryTransitioner(repositoryCloser, repositoryStateFetcher, retrier)

        retryingCloser.performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
    }
}
