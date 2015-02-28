package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryPromoter
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

@CompileStatic
public class PromoteRepositoryTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        RepositoryFetcher openRepositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryPromoter repositoryPromoter = createRepositoryPromoterWithGivenClient(createClient())

        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)
        //TODO: repositoryId should be taken from CloseRepositoryTask when executed
        String repositoryId = openRepositoryFetcher.getClosedRepositoryIdForStagingProfileId(stagingProfileId)
        repositoryPromoter.promoteRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }
}
