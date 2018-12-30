package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.RepositoryCreator
import org.gradle.api.Incubating
import org.gradle.api.tasks.TaskAction

@Incubating
@CompileStatic
@InheritConstructors(constructorAnnotations = true)
class CreateRepositoryTask extends BaseStagingTask {

    @TaskAction
    void createStagingRepositoryAndSaveItsId() {
        String stagingProfileId = getConfiguredStagingProfileIdOrFindAndCacheOne(createProfileFetcherWithGivenClient(createClient()))
        createRepositoryAndSaveItsId(stagingProfileId)
    }

    private String createRepositoryAndSaveItsId(String stagingProfileId) {
        RepositoryCreator repositoryCreator = createRepositoryCreatorWithGivenClient(createClient());
        String createdStagingRepositoryId = repositoryCreator.createStagingRepositoryAndReturnId(stagingProfileId);
        savePassedRepositoryIdForReusingInInOtherTasks(createdStagingRepositoryId)
        return createdStagingRepositoryId
    }

    private RepositoryCreator createRepositoryCreatorWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryCreator(client, getServerUrl())
    }
}
