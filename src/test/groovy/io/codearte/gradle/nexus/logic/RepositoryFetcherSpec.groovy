package io.codearte.gradle.nexus.logic

import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import io.codearte.gradle.nexus.infra.WrongNumberOfRepositories

class RepositoryFetcherSpec extends BaseOperationExecutorSpec implements FetcherResponseTrait {

    private static final String GET_REPOSITORY_ID_PATH = "/staging/profile_repositories/"
    private static final String GET_REPOSITORY_ID_FULL_URL = MOCK_SERVER_HOST + GET_REPOSITORY_ID_PATH + TEST_STAGING_PROFILE_ID

    private SimplifiedHttpJsonRestClient client
    private RepositoryFetcher fetcher

    void setup() {
        client = Mock()
        fetcher = new RepositoryFetcher(client, MOCK_SERVER_HOST)
    }

    def "should get open repository id from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([anOpenRepo()]) }
        when:
            String repositoryId = fetcher.getRepositoryIdWithGivenStateForStagingProfileId(TEST_STAGING_PROFILE_ID, RepositoryState.OPEN)
        then:
            repositoryId == TEST_REPOSITORY_ID
    }

    def "should get closed repository id from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([aClosedRepo()]) }
        when:
            String repositoryId = fetcher.getRepositoryIdWithGivenStateForStagingProfileId(TEST_STAGING_PROFILE_ID, RepositoryState.CLOSED)
        then:
            repositoryId == TEST_REPOSITORY_ID
    }

    def "should fail with meaningful exception on lack of repositories in given state #state"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([]) }
        when:
            fetcher.getRepositoryIdWithGivenStateForStagingProfileId(TEST_STAGING_PROFILE_ID, state)
        then:
            WrongNumberOfRepositories e = thrown()
            e.message == "Wrong number of received repositories in state '$state'. Expected 1, received 0".toString()
            e.numberOfRepositories == 0
            e.state == state.toString()
        where:
            state << [RepositoryState.OPEN, RepositoryState.CLOSED]
    }

    def "should fail with meaningful exception on too many repositories in given state #state"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> {
                createResponseMapWithGivenRepos([aRepoInStateAndId(TEST_REPOSITORY_ID, state),
                                                 aRepoInStateAndId(TEST_REPOSITORY_ID + "2", state)])
            }
        when:
            fetcher.getRepositoryIdWithGivenStateForStagingProfileId(TEST_STAGING_PROFILE_ID, state)
        then:
            WrongNumberOfRepositories e = thrown()
            e.message == "Wrong number of received repositories in state '$state'. Expected 1, received 2".toString()
            e.numberOfRepositories == 2
            e.state == state.toString()
        where:
            state << [RepositoryState.OPEN, RepositoryState.CLOSED]
    }

    def "should fail with meaningful exception on wrong repo state returned from server"() {
        given:
            client.get(GET_REPOSITORY_ID_FULL_URL) >> { createResponseMapWithGivenRepos([aRepoInState(receivedState)]) }
        when:
            fetcher.getRepositoryIdWithGivenStateForStagingProfileId(TEST_STAGING_PROFILE_ID, expectedState)
        then:
            WrongNumberOfRepositories e = thrown()
            e.message == "Wrong number of received repositories in state '$expectedState'. Expected 1, received 0".toString()
        where:
            expectedState          | receivedState
            RepositoryState.OPEN   | RepositoryState.CLOSED
            RepositoryState.CLOSED | RepositoryState.OPEN
    }

    private Map anOpenRepo() {
        return aRepoInStateAndId(TEST_REPOSITORY_ID, RepositoryState.OPEN)
    }

    private Map aClosedRepo() {
        return aRepoInStateAndId(TEST_REPOSITORY_ID, RepositoryState.CLOSED)
    }

    private Map aRepoInState(RepositoryState state) {
        return aRepoInStateAndId(TEST_REPOSITORY_ID, state)
    }
}
