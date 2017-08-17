package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import org.gradle.api.Incubating

@CompileStatic
@Slf4j
@Incubating
class RepositoryDropper extends AbstractRepositoryTransitioner {

    RepositoryDropper(SimplifiedHttpJsonRestClient client, String nexusUrl, String repositoryDescription) {
        super(client, nexusUrl, repositoryDescription)
    }

    @Override
    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Droping repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(pathForGivenBulkOperation("drop"), postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been accepted by server to be dropped")
    }

    @Override
    List<RepositoryState> desiredAfterTransitionRepositoryState() {
        throw new UnsupportedOperationException("Not implemented yet")
    }
}
