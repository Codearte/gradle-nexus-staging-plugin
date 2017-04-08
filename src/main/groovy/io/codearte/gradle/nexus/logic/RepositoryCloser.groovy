package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

@CompileStatic
@InheritConstructors
@Slf4j
class RepositoryCloser extends AbstractRepositoryTransitioner {

    @Deprecated
    void closeRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
    }

    @Override
    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Closing repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(pathForGivenBulkOperation("close"), postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' accepted by server to be closed")
    }

    @Override
    List<RepositoryState> desiredAfterTransitionRepositoryState() {
        return [RepositoryState.CLOSED]
    }
}
