package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.tasks.TaskAction

@CompileStatic
@InheritConstructors(constructorAnnotations = true)
class GetStagingProfileTask extends BaseStagingTask {

    @TaskAction
    void doAction() {
        StagingProfileFetcher stagingProfileFetcher = createProfileFetcherWithGivenClient(createClient())
        String receivedStagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
        logger.lifecycle("Received staging profile id: $receivedStagingProfileId")
        setStagingProfileId(receivedStagingProfileId)
    }
}
