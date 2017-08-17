package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

@CompileStatic
@Slf4j
class RepositoryCloser extends AbstractRepositoryTransitioner {

    RepositoryCloser(SimplifiedHttpJsonRestClient client, String nexusUrl, String repositoryDescription) {
        super(client, nexusUrl, repositoryDescription)
    }

    @Deprecated
    void closeRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        performWithRepositoryIdAndStagingProfileId(repositoryId, stagingProfileId)
    }

    @Override
    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Closing repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(pathForGivenBulkOperation("close"), postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been accepted by server to be closed")
    }

    @Override
    List<RepositoryState> desiredAfterTransitionRepositoryState() {
        return [RepositoryState.CLOSED]
    }
}
