package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

class GetStagingProfileTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        String receivedStagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
        logger.lifecycle("Received staging profile id: $receivedStagingProfileId")
        setStagingProfileId(receivedStagingProfileId)
    }
}
