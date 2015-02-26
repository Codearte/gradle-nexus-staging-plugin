package io.codearte.gradle.nexus

import groovy.transform.PackageScope
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryPromoter
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

abstract class BaseStagingTask extends DefaultTask {

    @Input
    String nexusUrl

    @Input
    @Optional
    String username

    @Input
    @Optional
    String password

    @Input
    String packageGroup

    @PackageScope
    SimplifiedHttpJsonRestClient createClient() {
        new SimplifiedHttpJsonRestClient(new RESTClient(), getUsername(), getPassword())
    }

    protected StagingProfileFetcher createFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new StagingProfileFetcher(client, getNexusUrl())
    }

    protected RepositoryFetcher createRepositoryFetcherWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryFetcher(client, getNexusUrl())
    }

    protected RepositoryCloser createRepositoryCloserWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryCloser(client, getNexusUrl())
    }

    protected RepositoryPromoter createRepositoryPromoterWithGivenClient(SimplifiedHttpJsonRestClient client) {
        return new RepositoryPromoter(client, getNexusUrl())
    }
}
