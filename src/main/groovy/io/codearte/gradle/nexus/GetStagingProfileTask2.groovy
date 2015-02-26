package io.codearte.gradle.nexus

import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

class GetStagingProfileTask2 extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createFetcherWithGivenClient(createClient())
        String stagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
        logger.info("Received staging profile id: $stagingProfileId")
        ext.stagingProfileId = stagingProfileId
    }
}
