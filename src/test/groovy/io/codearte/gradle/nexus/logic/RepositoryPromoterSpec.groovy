package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient

class RepositoryPromoterSpec extends BaseOperationExecutorSpec {

    private static final String PROMOTE_REPOSITORY_FULL_URL = pathForGivenBulkOperation("promote")

    def "should promote repository"() {
        given:
            SimplifiedHttpJsonRestClient client = Mock()
            RepositoryPromoter promoter = new RepositoryPromoter(client, MOCK_SERVER_HOST)
        when:
            promoter.performWithRepositoryIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(PROMOTE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
