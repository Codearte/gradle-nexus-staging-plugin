package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore

class RepositoryCloserSpec extends BaseOperationExecutorSpec {

    private static final String CLOSE_REPOSITORY_PATH = "service/local/staging/profiles/$TEST_STAGING_PROFILE_ID/finish"
    private static final String CLOSE_REPOSITORY_FULL_URL = MOCK_SERVER_HOST + CLOSE_REPOSITORY_PATH

    @Ignore
    def "should close repository e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def closer = new RepositoryCloser(client, "https://oss.sonatype.org/")
        when:
            closer.closeRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
    }

    def "should get open repository id from server"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            def closer = new RepositoryCloser(client, MOCK_SERVER_HOST)
        when:
            closer.closeRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(CLOSE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
