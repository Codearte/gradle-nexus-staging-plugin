package io.codearte.gradle.nexus.logic

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@InheritConstructors
@CompileStatic
abstract class AbstractRepositoryTransitioner extends BaseOperationExecutor implements RepositoryTransition {

    protected Map<String, Map> prepareStagingPostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryId: repositoryId,
                        description: 'Automatically released/promoted with gradle-nexus-staging-plugin!',
                        targetRepositoryId: stagingProfileId
                ]]
    }
}
