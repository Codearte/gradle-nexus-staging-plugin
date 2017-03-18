package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.WrongNumberOfRepositories

@InheritConstructors
@Slf4j
class RepositoryFetcher extends BaseOperationExecutor {

    String getOpenRepositoryIdForStagingProfileId(String stagingProfileId) {
        return getRepositoryIdWithGivenStateForStagingProfileId("open", stagingProfileId)
    }

    String getClosedRepositoryIdForStagingProfileId(String stagingProfileId) {
        return getRepositoryIdWithGivenStateForStagingProfileId("closed", stagingProfileId)
    }

    private String getRepositoryIdWithGivenStateForStagingProfileId(String state, String stagingProfileId) {
        log.info("Getting '$state' repository for staging profile '$stagingProfileId'")
        Map responseAsMap = client.get(nexusUrl + "/staging/profile_repositories/$stagingProfileId")    //TODO: Constant
        return parseResponseAndGetRepositoryIdInGivenState(responseAsMap, state)
    }

    private String parseResponseAndGetRepositoryIdInGivenState(Map responseAsMap, String repositoryState) {
        Map repository = verifyThatOneRepositoryAndReturnIt(responseAsMap, repositoryState)
        log.debug("Received 1 '$repositoryState' repository with id: ${repository.repositoryId}")
        return repository.repositoryId
    }

    private Map verifyThatOneRepositoryAndReturnIt(Map responseAsMap, String repositoryState) {
        Closure repositoryInGivenState = { it.type == repositoryState }
        int numberOfRepositories = responseAsMap.data.count(repositoryInGivenState)
        if (numberOfRepositories != 1) {
            throw new WrongNumberOfRepositories(numberOfRepositories, repositoryState)
        }
        return responseAsMap.data.find(repositoryInGivenState) as Map
    }
}
