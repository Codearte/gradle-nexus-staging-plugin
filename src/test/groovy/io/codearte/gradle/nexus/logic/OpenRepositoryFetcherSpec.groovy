package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore
import spock.lang.Specification

class OpenRepositoryFetcherSpec extends Specification {

    private static final String STAGING_PROFILE_ID = "93c08fdebde1ff"
    private static final String MOCK_SERVER_HOST = "https://mock.server/"
    private static final String GET_REPOSITORY_ID_PATH = "/service/local/staging/profile_repositories/"
    private static final String GET_REPOSITORY_ID_FULL_URL = MOCK_SERVER_HOST + GET_REPOSITORY_ID_PATH + STAGING_PROFILE_ID

    @Ignore
    def "should get open repository id from server e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def fetcher = new RepositoryFetcher(client, "https://oss.sonatype.org/")
        when:
            String stagingProfileId = fetcher.getOpenRepositoryIdForStagingProfileId(STAGING_PROFILE_ID)
        then:
            println stagingProfileId
            stagingProfileId == "iocodearte-1011"
    }

    def "should get open repository id from server"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            client.get(GET_REPOSITORY_ID_FULL_URL) >> {
                new JsonSlurper().parse(this.getClass().getResource("openRepositoryShrunkResponse.json"))
            }
            def fetcher = new RepositoryFetcher(client, MOCK_SERVER_HOST)
        when:
            String repositoryId = fetcher.getOpenRepositoryIdForStagingProfileId(STAGING_PROFILE_ID)
        then:
            println repositoryId
            repositoryId == "iocodearte-1011"
    }
}
