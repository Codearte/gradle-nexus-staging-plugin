package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryCloserSpec extends BaseOperationExecutorSpec {

    private static final String CLOSE_REPOSITORY_PATH = "/staging/profiles/$TEST_STAGING_PROFILE_ID/finish"
    private static final String CLOSE_REPOSITORY_FULL_URL = MOCK_SERVER_HOST + CLOSE_REPOSITORY_PATH

    def "should close open repository"() {
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
