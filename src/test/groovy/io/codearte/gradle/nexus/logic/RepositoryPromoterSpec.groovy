package io.codearte.gradle.nexus.logic

import groovy.json.JsonSlurper
import groovyx.net.http.RESTClient
import io.codearte.gradle.nexus.PasswordUtil
import io.codearte.gradle.nexus.infra.SimplifiedHttpJsonRestClient
import spock.lang.Ignore

class RepositoryPromoterSpec extends BaseOperationExecutorSpec {

    private static final String PROMOTE_REPOSITORY_PATH = "/staging/profiles/$TEST_STAGING_PROFILE_ID/promote"
    private static final String PROMOTE_REPOSITORY_FULL_URL = MOCK_SERVER_HOST + PROMOTE_REPOSITORY_PATH

    @Ignore
    def "should promote repository e2e"() {
        given:
            def client = new SimplifiedHttpJsonRestClient(new RESTClient(), "codearte", PasswordUtil.tryToReadNexusPassword())
            def promoter = new RepositoryPromoter(client, E2E_TEST_SERVER_BASE_PATH)
        when:
            promoter.promoteRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            noExceptionThrown()
    }

    def "should promote repository"() {
        given:
            def client = Mock(SimplifiedHttpJsonRestClient)
            def promoter = new RepositoryPromoter(client, MOCK_SERVER_HOST)
        when:
            promoter.promoteRepositoryWithIdAndStagingProfileId(TEST_REPOSITORY_ID, TEST_STAGING_PROFILE_ID)
        then:
            1 * client.post(PROMOTE_REPOSITORY_FULL_URL, _) >> { uri, content ->
                assert content == new JsonSlurper().parse(this.getClass().getResource("commonStagingRepositoryRequest.json"))
            }
    }
}
