package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore

class OpenRepositoryFetcherSpec extends BaseOperationExecutorSpec {

    private static final String GET_REPOSITORY_ID_PATH = "/staging/profile_repositories/"
    private static final String GET_REPOSITORY_ID_FULL_URL = MOCK_SERVER_HOST + GET_REPOSITORY_ID_PATH + TEST_STAGING_PROFILE_ID

    @Ignore
    def "should get open repository id from server e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def fetcher = new RepositoryFetcher(client, E2E_TEST_SERVER_BASE_PATH)
        when:
            String stagingProfileId = fetcher.getOpenRepositoryIdForStagingProfileId(TEST_STAGING_PROFILE_ID)
        then:
            println stagingProfileId
            stagingProfileId == TEST_REPOSITORY_ID
    }

    def "should get open repository id from server"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            client.get(GET_REPOSITORY_ID_FULL_URL) >> {
                new JsonSlurper().parse(this.getClass().getResource("openRepositoryShrunkResponse.json"))
            }
            def fetcher = new RepositoryFetcher(client, MOCK_SERVER_HOST)
        when:
            String repositoryId = fetcher.getOpenRepositoryIdForStagingProfileId(TEST_STAGING_PROFILE_ID)
        then:
            println repositoryId
            repositoryId == TEST_REPOSITORY_ID
    }
}
