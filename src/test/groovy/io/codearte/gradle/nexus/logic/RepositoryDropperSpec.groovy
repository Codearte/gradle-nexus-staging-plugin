package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryDropperSpec extends BaseOperationExecutorSpec {

    private static final String DROP_REPOSITORY_FULL_URL = pathForGivenBulkOperation("drop")

    def "should drop repository"() {
        given:
            SimplifiedHttpJsonRestClient client = Mock()
            RepositoryDropper dropper = new RepositoryDropper(client, MOCK_SERVER_HOST, TEST_REPOSITORY_DESCRIPTION)
        when:
            dropper.performWithRepositoryIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(DROP_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
