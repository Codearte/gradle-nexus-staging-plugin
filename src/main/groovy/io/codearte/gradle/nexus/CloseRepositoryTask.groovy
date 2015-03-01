package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

@CompileStatic
public class CloseRepositoryTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())  //Here or in fetchAndCacheStagingProfileId()?
        RepositoryFetcher repositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryCloser repositoryCloser = createRepositoryCloserWithGivenClient(createClient())
        OperationRetrier<String> retrier = createOperationRetrier()

        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)
        String repositoryId = retrier.doWithRetry { repositoryFetcher.getOpenRepositoryIdForStagingProfileId(stagingProfileId) }
        repositoryCloser.closeRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }
}
