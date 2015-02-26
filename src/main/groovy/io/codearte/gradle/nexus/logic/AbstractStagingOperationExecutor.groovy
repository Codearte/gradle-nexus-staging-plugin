package io.codearte.gradle.nexus.logic

import groovy.transform.InheritConstructors

@InheritConstructors
abstract class AbstractStagingOperationExecutor extends BaseOperationExecutor {

    protected Map prepareStagingPostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryId: repositoryId,
                        description: 'Automatically released/promoted with gradle-nexus-staging-plugin!',
                        targetRepositoryId: stagingProfileId
                ]]
    }
}
