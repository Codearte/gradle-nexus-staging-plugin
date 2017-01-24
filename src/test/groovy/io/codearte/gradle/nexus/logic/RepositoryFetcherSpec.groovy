package io.codearte.gradle.nexus.logic

import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.infra.WrongNumberOfRepositories
import spock.lang.Ignore

class RepositoryFetcherSpec extends BaseOperationExecutorSpec implements FetcherResponseTrait {

    private static final String GET_REPOSITORY_ID_PATH = "/staging/profile_repositories/"
    private static final String GET_REPOSITORY_ID_FULL_URL = MOCK_SERVER_HOST + GET_REPOSITORY_ID_PATH + TEST_STAGING_PROFILE_ID

    private SimplifiedHttpJsonRestClient client
    private RepositoryFetcher fetcher

    void setup() {
        client = Mock(SimplifiedHttpJsonRestClient)
        fetcher = new RepositoryFetcher(client, MOCK_SERVER_HOST)
    }

    @Ignore
    def "should get open repository id from server e2e"() {
        given:
            client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            fetcher = new RepositoryFetcher(client, E2E_TEST_SERVER_BASE_PATH)
        when:
            String stagingProfileId = fetcher.getOpenRepositoryIdForStagingProfileId(TEST_STAGING_PROFILE_ID)
        then:
            println stagingProfileId
            stagingProfileId == TEST_REPOSITORY_ID
    }

    def "should get open repository id from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([anOpenRepo()]) }
        when:
            String repositoryId = fetcher.getOpenRepositoryIdForStagingProfileId(TEST_STAGING_PROFILE_ID)
        then:
            repositoryId == TEST_REPOSITORY_ID
    }

    def "should get closed repository id from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([aClosedRepo()]) }
        when:
            String repositoryId = fetcher.getClosedRepositoryIdForStagingProfileId(TEST_STAGING_PROFILE_ID)
        then:
            repositoryId == TEST_REPOSITORY_ID
    }

    def "should fail with meaningful exception on lack of repositories in given state #state"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([]) }
        when:
            fetcher."get${state.capitalize()}RepositoryIdForStagingProfileId"(TEST_STAGING_PROFILE_ID)
        then:
            def e = thrown(WrongNumberOfRepositories)
            e.message == "Wrong number of received repositories in state '$state'. Expected 1, received 0".toString()
            e.numberOfRepositories == 0
            e.state == state
        where:
            state << ["open", "closed"]
    }

    def "should fail with meaningful exception on too many repositories in given state #state"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> {
                createResponseMapWithGivenRepos([aRepoInStateAndId(state, TEST_REPOSITORY_ID),
                                                 aRepoInStateAndId(state, TEST_REPOSITORY_ID + "2")])
            }
        when:
            fetcher."get${state.capitalize()}RepositoryIdForStagingProfileId"(TEST_STAGING_PROFILE_ID)
        then:
            def e = thrown(WrongNumberOfRepositories)
            e.message == "Wrong number of received repositories in state '$state'. Expected 1, received 2".toString()
            e.numberOfRepositories == 2
            e.state == state
        where:
            state << ["open", "closed"]
    }

    def "should fail with meaningful exception on wrong repo state returned from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([aRepoInState(receivedState)]) }
        when:
            fetcher."get${expectedState.capitalize()}RepositoryIdForStagingProfileId"(TEST_STAGING_PROFILE_ID)
        then:
            def e = thrown(IllegalArgumentException)
            e.message == "Unexpected state of received repository. Received $receivedState, expected $expectedState".toString()
        where:
            expectedState || receivedState
            "open"        || "closed"
            "closed"      || "open"
    }

    private Map anOpenRepo() {
        return aRepoInStateAndId("open", TEST_REPOSITORY_ID)
    }

    private Map aClosedRepo() {
        return aRepoInStateAndId("closed", TEST_REPOSITORY_ID)
    }

    private Map aRepoInState(String type) {
        return aRepoInStateAndId(type, TEST_REPOSITORY_ID)
    }
}
