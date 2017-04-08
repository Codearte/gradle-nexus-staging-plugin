package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryReleaser extends AbstractRepositoryTransitioner {

    private static final String RELEASE_OPERATION_NAME_IN_NEXUS = "promote"    //promote and release use the same operation, used parameters matter

    @Override
    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Releasing repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(pathForGivenBulkOperation(RELEASE_OPERATION_NAME_IN_NEXUS), postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been released")
    }

    @Override
    List<RepositoryState> desiredAfterTransitionRepositoryState() {
        return [RepositoryState.RELEASED, RepositoryState.NOT_FOUND]    //depending if "auto drop after release" is enabled
    }
}
