package io.codearte.gradle.nexus.logic

import groovy.transform.NotYetImplemented
import io.codearte.gradle.nexus.exception.UnsupportedRepositoryState
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryStateFetcherSpec extends BaseOperationExecutorSpec implements FetcherResponseTrait {

    private static final String GET_REPOSITORY_STATE_PATH = "/staging/repository/"

    private SimplifiedHttpJsonRestClient client
    private RepositoryStateFetcher repoStateFetcher

    void setup() {
        client = Mock(SimplifiedHttpJsonRestClient)
        repoStateFetcher = new RepositoryStateFetcher(client, MOCK_SERVER_HOST)
    }

    @SuppressWarnings("GrDeprecatedAPIUsage")
    def "should return state received from server mapped to enum"() {
        given:
            client.get(getGetRepositoryStateFullUrlForRepoId(TEST_REPOSITORY_ID)) >> { aRepoInStateAndId(TEST_REPOSITORY_ID, 'closed') }
        when:
            RepositoryState repoState = repoStateFetcher.getNonTransitioningRepositoryStateById(TEST_REPOSITORY_ID)
        then:
            repoState == RepositoryState.CLOSED
    }

    @SuppressWarnings("GrDeprecatedAPIUsage")
    def "should throw exception with meaningful message if unsupported or missing state"() {
        given:
            client.get(getGetRepositoryStateFullUrlForRepoId(TEST_REPOSITORY_ID)) >> { aRepoInStateAndId(TEST_REPOSITORY_ID, unsupportedState) }
        when:
            repoStateFetcher.getNonTransitioningRepositoryStateById(TEST_REPOSITORY_ID)
        then:
            UnsupportedRepositoryState e = thrown()
            e.unsupportedState == unsupportedState
        where:
            unsupportedState << ['completely wrong', null]
    }

    @NotYetImplemented
    def "should map 404 error from server to NOT_FOUND state"() {   //needed for 'drop' or 'dropAfterRelease' operations
    }

    private String getGetRepositoryStateFullUrlForRepoId(String repoId) {
        return MOCK_SERVER_HOST + GET_REPOSITORY_STATE_PATH + repoId
    }
}
