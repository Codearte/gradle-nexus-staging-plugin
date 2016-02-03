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

    String getPromotedRepositoryIdForStagingProfileId(String stagingProfileId) {
        return getRepositoryIdWithGivenStateForStagingProfileId("promoted", stagingProfileId)
    }

    private String getRepositoryIdWithGivenStateForStagingProfileId(String state, String stagingProfileId) {
        log.info("Getting '$state' repository for staging profile '$stagingProfileId'")
        Map responseAsMap = client.get(nexusUrl + "/staging/profile_repositories/$stagingProfileId")    //TODO: Constant
        return parseResponseAndGetRepositoryIdInGivenState(responseAsMap, state)
    }

    private String parseResponseAndGetRepositoryIdInGivenState(Map responseAsMap, String repositoryState) {
        def repository = verifyThatOneRepositoryAndReturnIt(responseAsMap, repositoryState)
        verifyReceivedRepositoryState(repository, repositoryState)
        log.debug("Received 1 '$repositoryState' repository with id: ${repository.repositoryId}")
        return repository.repositoryId
    }

    private Map verifyThatOneRepositoryAndReturnIt(Map responseAsMap, String repositoryState) {
        int numberOfRespositories = responseAsMap.data.size()
        if (numberOfRespositories != 1) {
            throw new WrongNumberOfRepositories(numberOfRespositories, repositoryState)
        }
        Map repository = responseAsMap.data[0] as Map
        return repository
    }

    private void verifyReceivedRepositoryState(Map repository, String expectedRepositoryState) {
        if (repository.type != expectedRepositoryState) {
            throw new IllegalArgumentException(
                    "Unexpected state of reveived repository. Received ${repository.type}, expected $expectedRepositoryState")
        }
    }
}
