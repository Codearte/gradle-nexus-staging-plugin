package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.internal.tasks.options.Option
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
        OperationRetrier<String> retrier = createOperationRetrier()

        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)
        String repositoryId = retrier.doWithRetry { repositoryFetcher.getOpenRepositoryIdForStagingProfileId(stagingProfileId) }
        stagingRepositoryId = repositoryId
        repositoryCloser.closeRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }
}
