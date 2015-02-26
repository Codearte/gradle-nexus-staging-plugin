package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@InheritConstructors
@Slf4j
class RepositoryFetcher extends BaseOperationExecutor {

    String getOpenRepositoryIdForStagingProfileId(String stagingProfileId) {
        log.info("Getting open repository for staging profile $stagingProfileId")
        Map responseAsMap = client.get(nexusUrl + "/service/local/staging/profile_repositories/$stagingProfileId")    //TODO: Constant
        return parseResponseAndGetRepositoryInGivenState(responseAsMap, "open")
    }

    String getClosedRepositoryIdForStagingProfileId(String stagingProfileId) {
        log.info("Getting closed repository for staging profile $stagingProfileId")
        Map responseAsMap = client.get(nexusUrl + "/service/local/staging/profile_repositories/$stagingProfileId")    //TODO: Constant
        return parseResponseAndGetRepositoryInGivenState(responseAsMap, "closed")
    }

    private String parseResponseAndGetRepositoryInGivenState(Map responseAsMap, String repositoryState) {
        //TODO: Better support for negative scenarios (e.g. filter by state/type)
        assert responseAsMap.data?.size() == 1
        assert responseAsMap.data[0].type == repositoryState
        def repositoryId = responseAsMap.data[0].repositoryId
        log.debug("Received 1 '$repositoryState' repository with id: $repositoryId")
        return repositoryId
    }
}
