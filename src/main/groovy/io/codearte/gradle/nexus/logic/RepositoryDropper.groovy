package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.gradle.api.Incubating

@CompileStatic
@InheritConstructors
@Slf4j
@Incubating
class RepositoryDropper extends AbstractRepositoryTransitioner {

    @Override
    void performWithRepositoryIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        log.info("Droping repository '$repositoryId' with staging profile '$stagingProfileId'")
        Map<String, Map> postContent = prepareStagingPostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        client.post(pathForGivenBulkOperation("drop"), postContent)
        log.info("Repository '$repositoryId' with staging profile '$stagingProfileId' has been dropped")
    }

    @Override
    List<RepositoryState> desiredAfterTransitionRepositoryState() {
        throw new UnsupportedOperationException("Not implemented yet")
    }
}
