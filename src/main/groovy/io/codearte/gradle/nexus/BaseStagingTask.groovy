package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryReleaser
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.DefaultTask
import org.gradle.api.Incubating
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

import javax.inject.Inject

@CompileStatic
@SuppressWarnings("UnstableApiUsage")
abstract class BaseStagingTask extends DefaultTask {

    @Input
    String serverUrl

    @Input
    @Optional
    String username

    @Input
    @Optional
    String password

    @Input
    String packageGroup

    @Input
    @Optional
    String stagingProfileId

    @Input
    Integer numberOfRetries

    @Input
    Integer delayBetweenRetriesInMillis

    @Input
    String repositoryDescription

    @Input
    @Optional
    @Incubating
    final Property<String> stagingRepositoryId

    @Inject
    BaseStagingTask(Project project, NexusStagingExtension extension) {
        ObjectFactory objectFactory = project.getObjects();
        stagingRepositoryId = objectFactory.property(String)
        stagingRepositoryId.set(extension.getStagingRepositoryId())
    }

    @PackageScope
    SimplifiedHttpJsonRestClient createClient() {
        return new SimplifiedHttpJsonRestClient(new RESTClient(), getUsername(), getPassword())
    }

    protected StagingProfileFetcher createProfileFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new StagingProfileFetcher(client, getServerUrl())
    }

    protected RepositoryFetcher createRepositoryFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryFetcher(client, getServerUrl())
    }

    protected RepositoryStateFetcher createRepositoryStateFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryStateFetcher(client, getServerUrl())
    }

    protected RepositoryCloser createRepositoryCloserWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryCloser(client, getServerUrl(), getRepositoryDescription())
    }

    protected RepositoryReleaser createRepositoryReleaserWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryReleaser(client, getServerUrl(), getRepositoryDescription())
    }

    protected <T> OperationRetrier<T> createOperationRetrier() {
        return new OperationRetrier<T>(getNumberOfRetries(), getDelayBetweenRetriesInMillis())
    }

    protected String getConfiguredStagingProfileIdOrFindAndCacheOne(StagingProfileFetcher stagingProfileFetcher) {
        String configuredStagingProfileId = getStagingProfileId()
        if (configuredStagingProfileId != null) {
            logger.info("Using configured staging profile id: $configuredStagingProfileId")
            return configuredStagingProfileId
        } else {
            String receivedStagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
            //TODO: Get from and set in plugin extension instead of in task directly
            setStagingProfileId(receivedStagingProfileId)
            return receivedStagingProfileId
        }
    }

    protected String getConfiguredRepositoryIdForStagingProfileOrFindAndCacheOneInGivenState(String stagingProfileId, RepositoryState repositoryState) {
        return tryToGetConfiguredRepositoryId().orElseGet {
            String repositoryId = findOneRepositoryIdInGivenStateForStagingProfileIdWithRetrying(repositoryState, stagingProfileId,
                createRepositoryFetcherWithGivenClient(createClient()))
            savePassedRepositoryIdForReusingInInOtherTasks(repositoryId)
            return repositoryId
        }
    }

    private java.util.Optional<String> tryToGetConfiguredRepositoryId() {
        //Provider doesn't not provide orElseGet()
        if (getStagingRepositoryId().isPresent()) {
            String reusedStagingRepositoryId = getStagingRepositoryId().get()
            logger.info("Reusing staging repository id: $reusedStagingRepositoryId")
            return java.util.Optional.of(reusedStagingRepositoryId)
        } else {
            return java.util.Optional.empty()
        }
    }

    private String findOneRepositoryIdInGivenStateForStagingProfileIdWithRetrying(RepositoryState repositoryState, String stagingProfileId,
                                                                                  RepositoryFetcher repositoryFetcher) {
        logger.warn("DEPRECATION WARNING. The staging repository ID is not provided. The fallback mode may impact release reliability and is deprecated. " +
            "Please consult the project FAQ how it can be fixed.")
        OperationRetrier<String> retrier = createOperationRetrier()
        return retrier.doWithRetry { repositoryFetcher.getRepositoryIdWithGivenStateForStagingProfileId(stagingProfileId, repositoryState) }
    }

    protected void savePassedRepositoryIdForReusingInInOtherTasks(String repositoryId) {
        logger.info("Saving repository ID $repositoryId for reusing in other tasks")
        NexusStagingExtension extension = getProject().getExtensions().getByType(NexusStagingExtension)
        extension.stagingRepositoryId.set(repositoryId)
    }
}
