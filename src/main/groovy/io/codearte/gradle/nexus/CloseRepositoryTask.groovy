package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

@CompileStatic
public class CloseRepositoryTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())  //Here or in fetchAndCacheStagingProfileId()?
        RepositoryFetcher openRepositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryCloser repositoryCloser = createRepositoryCloserWithGivenClient(createClient())

        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)
        String repositoryId = openRepositoryFetcher.getOpenRepositoryIdForStagingProfileId(stagingProfileId)
        repositoryCloser.closeRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }
}
