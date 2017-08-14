package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryReleaser
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CompileStatic
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
    @Optional
    Integer numberOfRetries

    @Input
    @Optional
    Integer delayBetweenRetriesInMillis

    @Input
    @Optional
    String repositoryDescription

    @PackageScope
    SimplifiedHttpJsonRestClient createClient() {
        new SimplifiedHttpJsonRestClient(new RESTClient(), getUsername(), getPassword())
    }

    protected StagingProfileFetcher createFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
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

    protected String fetchAndCacheStagingProfileId(StagingProfileFetcher stagingProfileFetcher) {
        String configuredStagingProfileId = getStagingProfileId()
        if (configuredStagingProfileId != null) {
            logger.info("Using configured staging profile id: $configuredStagingProfileId")
            return configuredStagingProfileId
        } else {
            String receivedStagingProfileId = stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
            setStagingProfileId(receivedStagingProfileId)
            return receivedStagingProfileId
        }
    }
}
