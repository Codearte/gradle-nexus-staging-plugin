package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@InheritConstructors
@Slf4j
class OpenRepositoryFetcher extends BaseOperationExecuter {

    String getOpenRepositoryIdForStagingProfileId(String stagingProfileId) {
        log.info("Getting open repository for staging profile $stagingProfileId")
        Map responseAsMap = client.get(nexusUrl + "/service/local/staging/profile_repositories/$stagingProfileId")    //TODO: Constant
        return parseResponseAndGetOpenRepository(responseAsMap)
    }

    private String parseResponseAndGetOpenRepository(Map responseAsMap) {
        //TODO: Better support for negative scenarios
        assert responseAsMap.data?.size() == 1
        assert responseAsMap.data[0].type == 'open'
        def repositoryId = responseAsMap.data[0].repositoryId
        log.debug("Received 1 open repository with id: $repositoryId")
        return repositoryId
    }
}
