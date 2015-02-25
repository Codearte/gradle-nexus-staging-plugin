package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

class GetStagingProfileTask2 extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        String stagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup("io.codearte")
        ext.stagingProfileId = stagingProfileId
    }

    private StagingProfileFetcher createFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new StagingProfileFetcher(client, nexusUrl)
    }
}
