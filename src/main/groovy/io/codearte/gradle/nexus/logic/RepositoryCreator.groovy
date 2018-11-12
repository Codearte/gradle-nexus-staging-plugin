package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

//@InheritConstructors  //TODO: Why broken???
@Slf4j
class RepositoryCreator extends BaseOperationExecutor {

    RepositoryCreator(SimplifiedHttpJsonRestClient client, String nexusUrl) {
        super(client, nexusUrl)
    }

    String createStagingRepositoryAndReturnId(String stagingProfileId) {
        log.info("Creating staging repository for staging profile '$stagingProfileId'")
        Map responseAsMap = client.post(nexusUrl + "/staging/profiles/${stagingProfileId}/start",
            [data: [description: "Explicitly created by gradle-nexus-staging-plugin"]])

        log.info("Raw response as map: $responseAsMap")
        String repositoryId = responseAsMap.data.stagedRepositoryId
        log.info("Created staging repository with id: $repositoryId")
        return repositoryId
    }
}
