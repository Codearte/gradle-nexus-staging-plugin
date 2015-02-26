package io.codearte.gradle.nexus.logic

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
//TODO: Duplication with RepositoryCloser and RepositoryDropper - StagingOperationExecuter?
class RepositoryPromoter extends BaseOperationExecutor {

    void promoteRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Promoting repository $repositoryId with staging profile $stagingProfileId")
        Map postContent = preparePostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        println new JsonBuilder(postContent).toString()
        client.post(nexusUrl + "service/local/staging/profiles/$stagingProfileId/promote", postContent)    //TODO: move service/local to URL
        log.info("Repository $repositoryId with staging profile $stagingProfileId has been promotted")
    }

    private Map preparePostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryId: repositoryId,
                        description: 'Automatically released/promoted with gradle-nexus-staging-plugin!',
                        targetRepositoryId: stagingProfileId
                ]]
    }
}
