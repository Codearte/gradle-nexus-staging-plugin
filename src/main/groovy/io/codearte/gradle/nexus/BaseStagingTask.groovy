package io.codearte.gradle.nexus

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryPromoter
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@CompileStatic
@Slf4j
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

    protected RepositoryCloser createRepositoryCloserWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryCloser(client, getServerUrl())
    }

    protected RepositoryPromoter createRepositoryPromoterWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryPromoter(client, getServerUrl())
    }

    protected String fetchAndCacheStagingProfileId(StagingProfileFetcher stagingProfileFetcher) {
        String configuredStagingProfileId = getStagingProfileId()
        if (configuredStagingProfileId != null) {
            log.info("Using configured staging profile id: $configuredStagingProfileId")
            return configuredStagingProfileId
        } else {
            //TODO: Set stagingProfileId for later reusage - will it work?
            return stagingProfileFetcher.getStagingProfileIdForPackageGroup(getPackageGroup())
        }
    }
}
