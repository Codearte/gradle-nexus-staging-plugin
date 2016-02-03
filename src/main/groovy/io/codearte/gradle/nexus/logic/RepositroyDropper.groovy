package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j;

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryDropper extends AbstractStagingOperationExecutor {

    void dropRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Dropping repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(nexusUrl + "/staging/profiles/$stagingProfileId/drop", postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been dropped")
    }
}

