package io.codearte.gradle.nexus.logic

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryCreator extends BaseOperationExecutor {

    String createStagingRepositoryAndReturnId(String stagingProfileId) {
        log.info("Creating staging repository for staging profile '$stagingProfileId'")
        //TODO: Pass package group (and root project name + version?) as description
        Map responseAsMap = client.post(nexusUrl + "/staging/profiles/${stagingProfileId}/start",
            [data: [description: "Explicitly created by gradle-nexus-staging-plugin"]])

        log.info("Raw response as map: $responseAsMap")
        String repositoryId = getStagingRepositoryIdFromResponseMap(responseAsMap)
        log.info("Created staging repository with id: $repositoryId")
        return repositoryId
    }

    @CompileDynamic
    private String getStagingRepositoryIdFromResponseMap(Map responseAsMap) {
        return responseAsMap.data.stagedRepositoryId
    }
}
