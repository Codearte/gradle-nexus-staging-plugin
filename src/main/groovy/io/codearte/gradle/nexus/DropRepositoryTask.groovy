package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryDropper
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

@CompileStatic
public class DropRepositoryTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        RepositoryFetcher repositoryFetcher = createRepositoryFetcherWithGivenClient(createClient())
        RepositoryDropper repositoryDropper = createRepositoryDropperWithGivenClient(createClient())
        OperationRetrier<String> retrier = createOperationRetrier()

        tryToTakeStagingProfileIdFromPromoteRepositoryTask()
        String stagingProfileId = fetchAndCacheStagingProfileId(stagingProfileFetcher)
        String repositoryId = retrier.doWithRetry { repositoryFetcher.getPromotedRepositoryIdForStagingProfileId(stagingProfileId) }
        repositoryDropper.dropRepositoryWithIdAndStagingProfileId(repositoryId, stagingProfileId)
    }

    private void tryToTakeStagingProfileIdFromPromoteRepositoryTask() {
        if (getStagingProfileId() != null) {
            return
        }
        String stagingProfileIdFromPromoteRepositoryTask = project.tasks.withType(PromoteRepositoryTask)[0].getStagingProfileId()
        if (stagingProfileIdFromPromoteRepositoryTask != null) {
            logger.debug("Reusing staging profile id from promoteRepository task: $stagingProfileIdFromPromoteRepositoryTask")
            setStagingProfileId(stagingProfileIdFromPromoteRepositoryTask)
        }
    }
}
