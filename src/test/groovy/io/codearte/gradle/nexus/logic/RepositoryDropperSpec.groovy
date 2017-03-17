package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore

class RepositoryDropperSpec extends BaseOperationExecutorSpec {

    private static final String DROP_REPOSITORY_PATH = "/staging/profiles/$TEST_STAGING_PROFILE_ID/drop"
    private static final String DROP_REPOSITORY_FULL_URL = MOCK_SERVER_HOST + DROP_REPOSITORY_PATH

    @Ignore
    def "should drop repository e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def dropper = new RepositoryDropper(client, E2E_TEST_SERVER_BASE_PATH)
        when:
            dropper.dropRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
    }

    def "should drop repository"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            def dropper = new RepositoryDropper(client, MOCK_SERVER_HOST)
        when:
            dropper.dropRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(DROP_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
