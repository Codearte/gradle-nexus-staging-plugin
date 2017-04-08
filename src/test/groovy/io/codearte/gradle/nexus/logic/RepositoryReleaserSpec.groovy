package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryReleaserSpec extends BaseOperationExecutorSpec {

    private static final String RELEASE_OPERATION_NAME = "promote"  //promote and release use the same operation, used parameters matter
    private static final String RELEASE_REPOSITORY_FULL_URL = pathForGivenBulkOperation(RELEASE_OPERATION_NAME)

    def "should release repository"() {
        given:
            SimplifiedHttpJsonRestClient client = Mock()
            RepositoryReleaser releaser = new RepositoryReleaser(client, MOCK_SERVER_HOST)
        when:
            releaser.performWithRepositoryIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(RELEASE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
