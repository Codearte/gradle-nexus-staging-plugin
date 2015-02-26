package io.codearte.gradle.nexus.logic

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryPromoter extends AbstractStagingOperationExecutor {

    void promoteRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Promoting repository $repositoryId with staging profile $stagingProfileId")
        Map postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        println new JsonBuilder(postContent).toString()
        client.post(nexusUrl + "/staging/profiles/$stagingProfileId/promote", postContent)
        log.info("Repository $repositoryId with staging profile $stagingProfileId has been promotted")
    }
}
