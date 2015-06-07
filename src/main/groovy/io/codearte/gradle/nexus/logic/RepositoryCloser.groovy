package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryCloser extends AbstractStagingOperationExecutor {

    void closeRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Closing repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(nexusUrl + "/staging/profiles/$stagingProfileId/finish", postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been closed")
    }
}
