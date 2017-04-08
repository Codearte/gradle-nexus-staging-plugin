package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryCloserSpec extends BaseOperationExecutorSpec {

    private static final String CLOSE_REPOSITORY_FULL_URL = pathForGivenBulkOperation("close")

    def "should close open repository"() {
        given:
            SimplifiedHttpJsonRestClient client = Mock()
            RepositoryCloser closer = new RepositoryCloser(client, MOCK_SERVER_HOST)
        when:
            closer.performWithRepositoryIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(CLOSE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
