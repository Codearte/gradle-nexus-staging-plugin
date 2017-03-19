package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.gradle.api.Incubating

@CompileStatic
@InheritConstructors
@Slf4j
@Incubating
class RepositoryDropper extends AbstractStagingOperationExecutor {

    void dropRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Droping repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(nexusUrl + "/staging/profiles/$stagingProfileId/drop", postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been dropped")
    }
}
