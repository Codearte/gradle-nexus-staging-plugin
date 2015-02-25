package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryPromoter
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

//TODO: Duplication with CloseRepositoryTask and DropRepositoryTask
public class PromoteRepositoryTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        RepositoryFetcher openRepositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryPromoter repositoryPromoter = createRepositoryPromoterWithGivenClient(createClient())

        String stagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
        String repositoryId = openRepositoryFetcher.getClosedRepositoryIdForStagingProfileId(stagingProfileId)
        repositoryPromoter.promoteRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }

    private StagingProfileFetcher createFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new StagingProfileFetcher(client, getNexusUrl())
    }

    private RepositoryFetcher createRepositoryFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryFetcher(client, getNexusUrl())
    }

    private RepositoryPromoter createRepositoryPromoterWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryPromoter(client, getNexusUrl())
    }
}
