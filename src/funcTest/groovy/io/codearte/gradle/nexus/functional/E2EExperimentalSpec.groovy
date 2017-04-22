package io.codearte.gradle.nexus.functional

import groovy.transform.NotYetImplemented
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.E2EFunctionalTestHelperTrait
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.logic.OperationRetrier
import io.codearte.gradle.nexus.logic.RepositoryCloser
import io.codearte.gradle.nexus.logic.RepositoryDropper
import io.codearte.gradle.nexus.logic.RepositoryFetcher
import io.codearte.gradle.nexus.logic.RepositoryReleaser
import io.codearte.gradle.nexus.logic.RepositoryState
import io.codearte.gradle.nexus.logic.RepositoryStateFetcher
import io.codearte.gradle.nexus.logic.RetryingRepositoryTransitioner
import io.codearte.gradle.nexus.logic.StagingProfileFetcher
import nebula.test.functional.ExecutionResult
import spock.lang.Ignore
import spock.lang.Stepwise

//TODO: Duplication with BasicFunctionalSpec done at Gradle level - decide which tests are better/easier to use and maintain
@Stepwise
@Ignore
class E2EExperimentalSpec extends BaseNexusStagingFunctionalSpec implements E2EFunctionalTestHelperTrait {

    private SimplifiedHttpJsonRestClient client
    private RepositoryStateFetcher repoStateFetcher
    private OperationRetrier<RepositoryState> retrier

    private static String resolvedStagingRepositoryId

    def setup() {
        client = new SimplifiedHttpJsonRestClient(new RESTClient(), nexusUsernameAT, nexusPasswordAT)
        repoStateFetcher = new RepositoryStateFetcher(client, E2E_SERVER_BASE_PATH)
        retrier = new OperationRetrier<>()
    }

    @NotYetImplemented
    def "remove all staging repositories if exist as clean up"() {}

    def "should get staging profile id from server e2e"() {
        given:
            StagingProfileFetcher fetcher = new StagingProfileFetcher(client, E2E_SERVER_BASE_PATH)
        when:
            String stagingProfileId = fetcher.getStagingProfileIdForPackageGroup(E2E_PACKAGE_GROUP)
        then:
            stagingProfileId == E2E_STAGING_PROFILE_ID
    }

    def "should upload artifacts to server"() {
        given:
            copyResources("sampleProjects//nexus-at-minimal", "")
        when:
            ExecutionResult result = runTasksSuccessfully('uploadArchives')
        then:
            result.standardOutput.contains('Uploading: io/gitlab/nexus-at/minimal/nexus-at-minimal/')
    }

    def "should get open repository id from server e2e"() {
        given:
            RepositoryFetcher fetcher = new RepositoryFetcher(client, E2E_SERVER_BASE_PATH)
        when:
            String stagingRepositoryId = fetcher.getOpenRepositoryIdForStagingProfileId(E2E_STAGING_PROFILE_ID)
        then:
            println stagingRepositoryId
            stagingRepositoryId.startsWith("iogitlabnexus-at")
        and:
            propagateStagingRepositoryIdToAnotherTest(stagingRepositoryId)
    }

    def "should get not in transition open repository state by repository id from server e2e"() {
        given:
            assert resolvedStagingRepositoryId
        when:
            RepositoryState receivedRepoState = repoStateFetcher.getNonTransitioningRepositoryStateById(resolvedStagingRepositoryId)
        then:
            receivedRepoState == RepositoryState.OPEN
    }

    def "should close open repository waiting for transition to finish e2e"() {
        given:
            assert resolvedStagingRepositoryId
        and:
            RepositoryCloser closer = new RepositoryCloser(client, E2E_SERVER_BASE_PATH)
            RetryingRepositoryTransitioner retryingCloser = new RetryingRepositoryTransitioner(closer, repoStateFetcher, retrier)
        when:
            retryingCloser.performWithRepositoryIdAndStagingProfileId(resolvedStagingRepositoryId, E2E_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
        and:
            RepositoryState receivedRepoState = repoStateFetcher.getNonTransitioningRepositoryStateById(resolvedStagingRepositoryId)
        then:
            receivedRepoState == RepositoryState.CLOSED
    }

    @Ignore //Not implemented yet
    def "should drop open repository e2e"() {
        given:
            assert resolvedStagingRepositoryId
        and:
            RepositoryDropper dropper = new RepositoryDropper(client, E2E_SERVER_BASE_PATH)
            RetryingRepositoryTransitioner retryingDropper = new RetryingRepositoryTransitioner(dropper, repoStateFetcher, retrier)
        when:
            retryingDropper.performWithRepositoryIdAndStagingProfileId(resolvedStagingRepositoryId, E2E_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
        when:
            RepositoryState receivedRepoState = repoStateFetcher.getNonTransitioningRepositoryStateById(resolvedStagingRepositoryId)
        then:
            receivedRepoState == RepositoryState.NOT_FOUND
    }

    def "should release closed repository e2e"() {
        given:
            assert resolvedStagingRepositoryId
        and:
            RepositoryReleaser releaser = new RepositoryReleaser(client, E2E_SERVER_BASE_PATH)
            RetryingRepositoryTransitioner retryingReleaser = new RetryingRepositoryTransitioner(releaser, repoStateFetcher, retrier)
        when:
            retryingReleaser.performWithRepositoryIdAndStagingProfileId(resolvedStagingRepositoryId, E2E_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
    }

    def "repository after release should be dropped immediately e2e"() {
        given:
            assert resolvedStagingRepositoryId
        when:
            RepositoryState receivedRepoState = repoStateFetcher.getNonTransitioningRepositoryStateById(resolvedStagingRepositoryId)
        then:
            receivedRepoState == RepositoryState.NOT_FOUND
    }

    private void propagateStagingRepositoryIdToAnotherTest(String stagingRepositoryId) {
        resolvedStagingRepositoryId = stagingRepositoryId
    }
}
