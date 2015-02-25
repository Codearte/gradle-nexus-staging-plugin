package io.codearte.gradle.nexus.logic

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors

@CompileStatic
@InheritConstructors
class RepositoryCloser extends BaseOperationExecuter {

    void closeRepositoryWithIdAndStagingProfileId(String repositoryId, String stagingProfileId) {
        Map postContent = preparePostContentWithGivenRepositoryIdAndStagingId(repositoryId, stagingProfileId)
        println new JsonBuilder(postContent).toString()
        client.post(nexusUrl + "service/local/staging/profiles/$stagingProfileId/finish", postContent)    //TODO: move service/local to URL
    }

    private Map preparePostContentWithGivenRepositoryIdAndStagingId(String repositoryId, String stagingProfileId) {
        return [data: [
                        stagedRepositoryId: repositoryId,
                        description: 'unknown:package:0.0.1',
                        targetRepositoryId: stagingProfileId
                ]]
    }
}
